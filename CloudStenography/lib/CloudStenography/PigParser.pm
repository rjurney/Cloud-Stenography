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

our $dataset = 'A';

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
    my $dataset = 'A';
    
    # This holds the value of the last dataset.
    my $last_dataset;
    # Loop through the directed graph, and create a command line for each node.
    foreach my $t (@{$ts})
    {
        # It is the job of each command to increment the dataset counter as needed,
        # and to set the last dataset.
        my $command;
        ($command, $dataset, $last_dataset) = $self->parse_command($dataset, $last_dataset, $nodes->{$t}->{name}, $nodes->{$t}->{value}, $mode);
        
        push @commands, $command;
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
    my ( $self, $dataset, $last_dataset, $name, $value, $mode ) = @_;
    
    carp "Must provide name of command and value!" unless $dataset and $name;

    my $command;
    
    if($name eq 'LOAD')
    {
        my $filename = $value->{filename};
        
        # Hard coded apache for now
        $command = "$dataset = LOAD '$PATH$filename' USING LogLoader as (remoteAddr, remoteLogname, user, time, method, uri, proto, status, bytes, referer, userAgent);\n";
    }
    elsif($name eq 'FILTER')
    {
        my $filter = $value->{filter};

        $command = "$dataset = FILTER $last_dataset BY $filter;\n";
    }
    elsif($name eq 'STORE')
    {
        my $filename = $value->{filename};
        
        if($mode eq 'run')
        {
            $command = "STORE " . $last_dataset . " INTO '" . $filename . "';";
        }
        elsif($mode eq 'illustrate')
        {
            $command = "ILLUSTRATE $last_dataset;";
        }
    }
    
    $last_dataset = $dataset;
    $dataset++;
    
    return ($command, $dataset, $last_dataset);
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
    
    my $cmd = '/Users/peyomp/Projects/cloudsteno/CloudStenography/pig-0.3.0/bin/pig -x local';
    my ($in, $out, $err);
    
    warn dump($commands);
    
    run3 $cmd, $commands, \$out, \$err;
    
    warn "Output: $out";
}

sub run_command
{
    my ( $self, $commands ) = @_;
    
    
}

1;