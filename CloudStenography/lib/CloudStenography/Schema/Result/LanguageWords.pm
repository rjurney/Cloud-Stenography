package CloudStenography::Schema::Result::LanguageWords;

use strict;
use warnings;

use base 'DBIx::Class';

__PACKAGE__->load_components("InflateColumn::DateTime", "Core");
__PACKAGE__->table("pig.language_words");
__PACKAGE__->add_columns(
  "id",
  {
    data_type => "integer",
    default_value => "nextval('pig.language_words_id_seq'::regclass)",
    is_nullable => 0,
    size => 4,
  },
  "name",
  {
    data_type => "character varying",
    default_value => undef,
    is_nullable => 0,
    size => 255,
  },
  "working",
  {
    data_type => "text",
    default_value => undef,
    is_nullable => 0,
    size => undef,
  },
  "language",
  {
    data_type => "character varying",
    default_value => undef,
    is_nullable => 0,
    size => 255,
  },
);
__PACKAGE__->set_primary_key("id");
__PACKAGE__->add_unique_constraint("language_words_pkey", ["id"]);


# Created by DBIx::Class::Schema::Loader v0.04006 @ 2009-07-05 18:47:51
# DO NOT MODIFY THIS OR ANYTHING ABOVE! md5sum:pC0b7BaglW2bKZTeQphg9Q


# You can replace this text with custom content, and it will be preserved on regeneration
1;
