package CloudStenography::Controller::Excel;

use strict;
use warnings;
use base 'Catalyst::Controller';

use Data::Dump qw/dump/;

use CloudStenography::Obj2Excel;

sub index :Path :Args(0) {
    my ( $self, $c ) = @_;
    
    $c->log->debug("Foo!");
    
    my $excel = CloudStenography::Obj2Excel->new();

    open FILE, "excel/myoutput";
    my @lines = <FILE>;

    foreach my $line (@lines)
    {
        my @data = split(/\t/, $line);
        warn "Line: " . dump(@data);
        $excel->add_data(@data);
    }

    $c->response->content_type('application/vnd.ms-excel');
    $c->response->body($excel->render);    
}

sub default :Path {
    my ( $self, $c ) = @_;
    $c->response->body( 'Page not found' );
    $c->response->status(404);
}

1;
