#
# AdventREST::Schema.pm
#
# $Id: $

package AdventREST::Schema;
use base qw/DBIx::Class::Schema/;

__PACKAGE__->load_classes(qw/User/);

1;

