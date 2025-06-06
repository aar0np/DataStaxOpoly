CREATE TABLE tokens (
token_id int PRIMARY KEY,
name text);

CREATE TABLE swag_cards (
card_id int PRIMARY KEY,
name text,
type text,
value int,
special text);

CREATE TABLE squares (
square_id int PRIMARY KEY,
name text,
image text,
price int,
type text,
rent LIST<int>,
rent_database int,
mortgage int,
structure_cost int,
center_x int,
center_y int,
special text
);

CREATE TABLE properties_by_player (
game_id UUID,
player_id UUID,
square_id int,
name text,
image text,
price int,
type text,
rent LIST<int>,
rent_database int,
mortgage int,
structure_cost int,
special text,
PRIMARY KEY (game_id,square_id)
) WITH transactional_mode = 'full';

CREATE TABLE community_cards (
card_id int PRIMARY KEY,
name text,
type text,
value int,
special text
);

CREATE TABLE jail (
game_id UUID,
player_id UUID,
name text,
turns_remaining_in_jail int,
PRIMARY KEY (game_id,player_id)
);

CREATE TABLE players (
game_id uuid,
player_id uuid,
cash int,
name text,
square_id int,
token_color text,
token_id int,
get_out_of_jail_cards map<text, int>,
PRIMARY KEY (game_id, player_id)
) WITH transactional_mode = 'full';

CREATE TABLE games (
game_id UUID PRIMARY KEY,
game_name text,
active boolean,
accepting_players boolean
);

CREATE TABLE player_login (
player_id uuid,
player_name text PRIMARY KEY,
password text
);
