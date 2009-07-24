package CloudStenography::PigParser;

use Moose;
use Carp;
use Data::Dump qw/dump/;

use JSON;

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
    
    warn "Illustrating " . $properties->{name} . ": " . $properties->{descriptions};
    
    my @commands = $self->initialize_pig();
    
    my $link_counter = 0; my $last_dataset;
    # Loop through each module and build a line of command for it.
    foreach my $module (@{$modules})
    {
        push @commands, $self->parse_command(\$dataset, $module->{name}, $module->{value});
    }
    
    return \@commands;
}

=head2 parse_wires

Ok, so a simpel ordering is not gonna work - can't trust the ordering of the array.
Have to either order simple array by links - parsing them, or create a network graph
and walk it.  Probably 1, then the other.

=cut

sub parse_wires
{
    my ( $self, $wires ) = @_;
    
    return unless $wires;
    
    # Loop through the wires
    foreach my $wire (@{$wires})
    {
        
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