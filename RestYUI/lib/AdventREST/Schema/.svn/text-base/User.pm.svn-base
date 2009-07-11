#
# AdventREST::Schema::User.pm
#
# $Id: $

package AdventREST::Schema::User;

use base qw/DBIx::Class/;

__PACKAGE__->load_components(qw/Core/);
__PACKAGE__->table('user');
__PACKAGE__->add_columns(qw/user_id fullname description/);
__PACKAGE__->set_primary_key('user_id');

1;

