package CloudStenography::Controller::SMD;

use strict;
use warnings;
use parent 'Catalyst::Controller';

=head1 NAME

CloudStenography::Controller::SMD - Catalyst Controller

=head1 DESCRIPTION

Catalyst Controller.

=head1 METHODS

=cut


=head2 index

=cut

sub index :Path :Args(0) {
    my ( $self, $c ) = @_;

    $c->stash->{template} = 'CloudStenography.t';
    
    $c->forward('View::TT');
}


=head1 AUTHOR

Peyompian

=head1 LICENSE

This library is free software. You can redistribute it and/or modify
it under the same terms as Perl itself.

=cut

1;
