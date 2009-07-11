package AdventREST::Model::DB;

use strict;
use base 'Catalyst::Model::DBIC::Schema';

__PACKAGE__->config(
    schema_class => 'AdventREST::Schema',
);

=head1 NAME

AdventREST::Model::DB - Catalyst DBIC Schema Model

=head1 SYNOPSIS

See L<AdventREST>

=head1 DESCRIPTION

L<Catalyst::Model::DBIC::Schema> Model using schema L<AdventREST::Schema>

=head1 AUTHOR

Adam Jacob L<mailto:adam@stalecoffee.org>

=head1 LICENSE

This library is free software, you can redistribute it and/or modify
it under the same terms as Perl itself.

=cut

1;
