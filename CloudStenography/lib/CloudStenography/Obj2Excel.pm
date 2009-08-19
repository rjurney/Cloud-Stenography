package CloudStenography::Obj2Excel;

use strict;
use warnings;
use Carp;
use Data::Dump qw( dump );
use Spreadsheet::WriteExcel::Simple;

sub no_header { 1 };

sub new
{
    my $class = shift;
    my $self  = {};
    bless($self, $class);
    $self->_init(@_);
    return $self;
}

sub _init
{
    my $self = shift;
    $self->{_start} = time();
    if (@_)
    {
        my %extra = @_;
        @$self{keys %extra} = values %extra;
    }

    $self->{ss} = Spreadsheet::WriteExcel::Simple->new;

    $self->template($self->{template}) if $self->{template};

	# Sometimes we don't want the header
    $self->{_header_written} = 1 if $self->{no_header};

}

sub add_data
{
    my $self = shift;
    my @data = @_;
    
    # Prevent Excel from making urls by prepending '
    foreach my $line (@data)
    {
	$line = '\'' . $line if $line =~ /^http/;
	#$line = "<a href=\"$line\">" . $line . "</a>" if $line =~ /^http/;
    }
    
    #$self->write_header unless $self->{_header_written};
    $self->{ss}->write_row(\@data);
}

=head2 render

Returns the Excel doc ready for printing.

=cut

sub render { return $_[0]->{ss}->data }

=head2 write( I<filename> )

Write the Excel doc to disk.

=cut

sub write
{
    my $self = shift;
    return $self->{ss}->save(@_);
}

1;
