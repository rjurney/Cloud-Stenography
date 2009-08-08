package CloudStenography::PigParser;

use Moose;
use Carp;
use Data::Dump qw/dump/;

use JSON;

# This is used to compute a valid order to generate/run our Pig commands in.
# Pig Latin itself is a Directed Acyclic Graph.
use Graph;
use Graph::Directed;

# This handles interaction with Pig via the command line.  Embedded Pig may make
# sense soon via Java calls.
use IPC::Run3;

my $PATH = '/Users/rjurney/Projects/cloudsteno/CloudStenography/pig-0.3.0/';

our $letter = 'A';
our %node_letters;

open my $REAL_STDIN, "<&=".fileno(*STDIN);
open my $REAL_STDOUT, ">>&=".fileno(*STDOUT);

sub parse_json {
    
    my ( $self, $json, $mode ) = @_;
    
    carp "Need JSON!" unless $json;
    
    my $struct = decode_json($json);
    
    warn dump($struct);
    
    carp "Bad JSON structure mang!"
        unless $struct->{properties} and $struct->{modules} and $struct->{wires};

    my $modules = $struct->{modules};
    my $properties = $struct->{properties};
    my $wires = $struct->{wires};
    
    my @commands = $self->initialize_pig();
    
    my ($ts, $nodes, $graph) = $self->create_graph($wires, $modules);
    
    # This is the name of the dataset at each step of the dataflow.
    my $letter = 'A';
    
    # This holds the value of the last dataset.
    my $last_letter;
    warn "TS: " . dump($ts);
    # Loop through the directed graph, and create a command line for each node.
    foreach my $t (@{$ts})
    {
        warn "T: $t";
        # It is the job of each command to increment the dataset counter as needed,
        # and to set the last dataset.
        my $command;
        ($command, $letter, $last_letter) = $self->parse_command($letter, $last_letter, $nodes->{$t}->{name}, $nodes->{$t}->{value}, $mode, $graph, $t);
        
        push @commands, $command;
        $node_letters{$t} = $last_letter;
    }
    
    push @commands, "quit;\n";
    
    return \@commands;
}

=head2 create_graph

Given the nodes (vertices) and edges (wires), create a Graph::Directed that will
give us a path to iterate over to produce a wholesome data value iterator at
each step and flow the data properly in Pig Latin.

Returns three things: an array of integers, that are ordered in the correct order for the script
to be run, as the dataset indicator iterates, and a hash of the nodes, keyed by
their id in the ordering array, and finally the graph object itself.

=cut

sub create_graph
{
    my ( $self, $wires, $nodes ) = @_;
    
    carp "Both wires and nodes are not optional!" unless $wires and $nodes;
    
    # Create a DAG (Directed, Acyclic Graph - what a Pig Latin script is).
    my $dag = Graph::Directed->new();
    
    my %nodes;
    my $counter = 0;
    # Index the modules by a hash keyed by numbers
    foreach my $n (@{$nodes})
    {
        $nodes{$counter} = $n;
        $counter++;
    }
    
    my $i = 0;
    # Loop through the wires
    foreach my $wire (@{$wires})
    {
        my $src = $wire->{src}->{moduleId};
        my $tgt = $wire->{tgt}->{moduleId};
        
        $dag->add_edge($src, $tgt);
    }
    
    my @ts = $dag->topological_sort;
    
    return (\@ts, \%nodes, $dag);
}

=head2 parse_command

Parse the data and return a Pig command, the next counter and the current counter for this line.

=cut
sub parse_command
{
    my ( $self, $letter, $last_letter, $name, $value, $mode, $graph, $graph_node ) = @_;
    
    carp "Must provide name of command and value!" unless $letter and $name;

    my $command;
    
    if($name eq 'LOAD')
    {
        my $filename = $value->{filename};
        
        # Hard coded apache for now
        $command = "$letter = LOAD '$PATH$filename' USING LogLoader as (remoteAddr, remoteLogname, user, time, method, uri, proto, status, bytes, referer, userAgent);\n";
    }
    elsif($name eq 'FILTER')
    {
        my $filter = $value->{filter};

        $command = "$letter = FILTER $last_letter BY $filter;\n";
    }
    elsif($name eq 'STORE')
    {
        my $filename = $value->{filename};
        
        if($mode eq 'run')
        {
            $command = "STORE $last_letter INTO '$filename';";
        }
        elsif($mode eq 'illustrate')
        {
            $command = "ILLUSTRATE $last_letter;";
        }
    }
    elsif($name eq 'JOIN')
    {
        my $cond_a = $value->{cond_a};
        my $cond_b = $value->{cond_b};
        
        my @preds = $graph->predecessors($graph_node);
        
        warn "Preds: " . dump(@preds);
        
        my $letter1 = $node_letters{$preds[0]};
        my $letter2 = $node_letters{$preds[1]};
        
        $command = "$letter = JOIN $letter1 BY $cond_a, $letter2 by $cond_b;";
    }
    
    $last_letter = $letter;
    $letter++;
    
    return ($command, $letter, $last_letter);
}

sub initialize_pig
{
    my ( $self ) = @_;
    
    my @commands =  (   # Hard coding apache for now
                        "register /Users/peyomp/Projects/cloudsteno/CloudStenography/pig-0.3.0/contrib/piggybank/java/piggybank.jar;\n",
                        "DEFINE LogLoader org.apache.pig.piggybank.storage.apachelog.CombinedLogLoader();\n",
                        "DEFINE DayExtractor org.apache.pig.piggybank.evaluation.util.apachelogparser.DateExtractor('yyyy-MM-dd');\n",
                    );
}

sub illustrate_commands
{
    my ( $self, $commands ) = @_;
    
    local *STDIN = $REAL_STDIN;   # restore the real ones so the filenos
    local *STDOUT = $REAL_STDOUT; # are 0 and 1 for the env setup

    my $cmd = '/Users/peyomp/Projects/cloudsteno/CloudStenography/pig-0.3.0/bin/pig -x local 2>/dev/null';
    my @out; my @err;
    
    run3 $cmd, $commands, \@out, \@err;
    
    # Simple debug.
    foreach my $outline (@out)
    {
        next if $outline =~ /^grunt/;
        next if $outline =~ /^---------/;
        next if $outline =~ /^\n/;
        
        warn $outline;
    }
    
    # Go through, and categorize each by their data name.
    my $data_names = {};
    my $letter;
    foreach my $outline (@out)
    {
        warn "Outline: $outline";
        
        next if $outline =~ /^grunt/;
        next if $outline =~ /^---------/;
        next if $outline =~ /^\n/;
        
        my @lineparts = split(/ \| /, $outline);
        
        foreach my $lp (@lineparts)
        {
            $lp =~ s/^\s+//;
            $lp =~ s/\s+$//;
        }
        
        if($lineparts[0] =~ /\^| \w/)
        {
            # We are a data definition row.  Get the letter and then skip for now.
            $letter = $lineparts[0];
            $letter =~ s/^\| //;
        }
        else
        {
            # Kill the first item.
            shift(@lineparts);
            
            # Set array ref if not set.
            #$data_names->{$letter} = [] unless $data_names->{$letter};
            
            # Push lineparts ref to this letter's data.
            push @{$data_names->{$letter}}, \@lineparts;
        }
        
        warn "Outline: " . dump(@lineparts);
    }
    
    warn dump($data_names);
}

sub run_command
{
    my ( $self, $commands ) = @_;
    
    
}

1;