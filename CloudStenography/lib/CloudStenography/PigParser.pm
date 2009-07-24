package CloudStenography::PigParser;

use Moose;
use Carp;
use Data::Dump qw/dump/;

use JSON;

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
    
    my @commands; my $dataset = 'A'; my $link_counter = 0;
    # Loop through each module and build a line of command for it.
    foreach my $module (@{$modules})
    {
        unless($module->{name} eq 'STORE')
        {
            push @commands, "$dataset = " . $self->parse_command($module->{name}, $module->{value});
        }
        else
        {
            push @commands, "STORE $dataset";
        }
        $dataset++; $link_counter++;
    }
    
    return \@commands;
}

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
    my ( $self, $name, $value ) = @_;
    
    carp "Must provide name of command and value!" unless $name;
    
    if($name eq 'LOAD')
    {
        return "LOAD $value;"
    }
}

1;