package CloudStenography::Model::CloudDB;

use strict;
use base 'Catalyst::Model::DBIC::Schema';

__PACKAGE__->config(
    schema_class => 'CloudStenography::Schema',
    connect_info => {
        dsn => 'dbi:Pg:dbname=cloudstenography',
        user => 'postgres',
        password => ''
  }


);

=head1 NAME

CloudStenography::Model::CloudDB - Catalyst DBIC Schema Model
=head1 SYNOPSIS

See L<CloudStenography>

=head1 DESCRIPTION

L<Catalyst::Model::DBIC::Schema> Model using schema L<CloudStenography::Schema>

=head1 AUTHOR

Peyompian

=head1 LICENSE

This library is free software, you can redistribute it and/or modify
it under the same terms as Perl itself.

=cut

1;
