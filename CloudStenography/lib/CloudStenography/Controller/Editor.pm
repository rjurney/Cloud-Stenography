package CloudStenography::Controller::Editor;

use strict;
use warnings;
use base 'Catalyst::Controller';

=head1 NAME

CloudStenography::Controller::Editor - Catalyst Controller

=head1 DESCRIPTION

Catalyst Controller.

=head1 METHODS

=cut


=head2 index

=cut

sub index :Path :Args(0) {
    my ( $self, $c ) = @_;
    
    $c->stash->{template} = 'jsBox.tt';
    
    $c->forward('View::TT');

}

sub js :Local
{
    my ( $seld, $c ) = @_;
    
    $c->stash->{template} = 'jsBox.js.tt';
    
    $c->forward('View::TT');
}




=head1 AUTHOR

Russell Jurney

=head1 LICENSE

This library is free software. You can redistribute it and/or modify
it under the same terms as Perl itself.

=cut

1;
