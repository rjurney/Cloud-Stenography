package CloudStenography::PigParser;

use Moose;
use Carp;
use Data::Dump qw/dump/;

use JSON;
use Graph;
use Graph::Directed;

my $PATH = '/Users/rjurney/Projects/cloudsteno/CloudStenography/pig-0.3.0/';

our $dataset = 'A';

sub parse_json {
    
    my ( $self, $json ) = @_;
    
    carp "Need JSON!" unless $json;
    
    my $struct = decode_json($json);
    
    warn dump($struct);
    
    carp "Bad JSON structure mang!"
        unless $struct->{properties} and $struct->{modules} and $struct->{wires};

    my $modules = $struct->{modules};
    my $properties = $struct->{properties};
    my $wires = $struct->{wires};
    
    my @commands = $self->initialize_pig();
    
    my $graph = $self->create_graph($wires, $modules);
    
    #my $link_counter = 0; my $last_dataset;
    # Loop through each module and build a line of command for it.
    #foreach my $module (@{$modules})
    #{
    #    push @commands, $self->parse_command(\$dataset, $module->{name}, $module->{value});
    #}
    
    return \@commands;
}

=head2 create_graph

Given the nodes (vertices) and edges (wires), create a Graph::Directed that will
give us a path to iterate over to produce a wholesome data value iterator at
each step and flow the data properly in Pig Latin.

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
    
    warn "The graph is: $dag\n";
    
    my @ts = $dag->topological_sort;
     
    warn dump(@ts);
    
    foreach my $t (@ts)
    {
        warn "Order: $t" . dump($nodes{$t});
    }
}

sub parse_command
{
    my ( $self, $datasetref, $name, $value ) = @_;
    
    carp "Must provide name of command and value!" unless $datasetref and $name;
    
    my $dataset = $$datasetref;
    
    if($name eq 'LOAD')
    {
        my $filename = $value->{filename};
        # Hard coded apache for now
        return "$dataset = LOAD '$PATH$filename' USING LogLoader as (remoteAddr, remoteLogname, user, time, method, uri, proto, status, bytes, referer, userAgent);";
        $dataset++;
    }
    elsif($name eq 'FILTER')
    {
        my $filter = $value->{filter};
        
        my $last_dataset = $dataset;
        $dataset++;
        return "$dataset = FILTER $last_dataset BY $filter;"
    }
    elsif($name eq 'STORE')
    {
        my $filename = $value->{filename};
        
        return "STORE " . $dataset . " INTO '" . $filename . "';";
    }
}

sub initialize_pig
{
    my ( $self ) = @_;
    
    my @commands =  (   # Hard coding apache for now
                        'register /Users/peyomp/Projects/cloudsteno/CloudStenography/pig-0.3.0/contrib/piggybank/java/piggybank.jar;',
                        'DEFINE LogLoader org.apache.pig.piggybank.storage.apachelog.CombinedLogLoader();',
                        "DEFINE DayExtractor org.apache.pig.piggybank.evaluation.util.apachelogparser.DateExtractor('yyyy-MM-dd');",
                    );
}

1;