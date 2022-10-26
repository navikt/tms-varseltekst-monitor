create table coalescing_rule(
    id serial primary key,
    name text unique,
    description text,
    created_at timestamp without time zone
);

create table coalescing_history_web_tekst(
    rule_id int references coalescing_rule(id),
    old_tekst_id int references web_tekst(id),
    new_tekst_id int references web_tekst(id),
    applied_at timestamp without time zone
);

create table coalescing_history_sms_tekst(
    rule_id int references coalescing_rule(id),
    old_tekst_id int references sms_tekst(id),
    new_tekst_id int references sms_tekst(id),
    applied_at timestamp without time zone
);

create table coalescing_history_epost_tittel(
    rule_id int references coalescing_rule(id),
    old_tekst_id int references epost_tittel(id),
    new_tekst_id int references epost_tittel(id),
    applied_at timestamp without time zone
);

create table coalescing_history_epost_tekst(
    rule_id int references coalescing_rule(id),
    old_tekst_id int references epost_tekst(id),
    new_tekst_id int references epost_tekst(id),
    applied_at timestamp without time zone
);

create table coalescing_backlog(
    id serial primary key,
    tekst_table text,
    rule_id int references coalescing_rule(id),
    tekst_id int
);

alter table web_tekst add column first_seen_at timestamp without time zone;
alter table sms_tekst add column first_seen_at timestamp without time zone;
alter table epost_tittel add column first_seen_at timestamp without time zone;
alter table epost_tekst add column first_seen_at timestamp without time zone;

create index coalescing_history_web_tekst_applied_at on web_tekst(first_seen_at);
create index coalescing_history_sms_tekst_applied_at on sms_tekst(first_seen_at);
create index coalescing_history_epost_tittel_applied_at on epost_tittel(first_seen_at);
create index coalescing_history_epost_tekst_applied_at on epost_tekst(first_seen_at);

with web_tekst_seen(id, first_seen) as
    ( select web_tekst, min(tidspunkt) from varsel group by web_tekst )
update web_tekst wt set first_seen_at = web_tekst_seen.first_seen from web_tekst_seen where wt.id = web_tekst_seen.id;

with sms_tekst_seen(id, first_seen) as
    ( select sms_tekst, min(tidspunkt) from varsel group by sms_tekst )
update sms_tekst st set first_seen_at = sms_tekst_seen.first_seen from sms_tekst_seen where st.id = sms_tekst_seen.id;

with epost_tittel_seen(id, first_seen) as
    ( select epost_tittel, min(tidspunkt) from varsel group by epost_tittel )
update epost_tittel et set first_seen_at = epost_tittel_seen.first_seen from epost_tittel_seen where et.id = epost_tittel_seen.id;

with epost_tekst_seen(id, first_seen) as
    ( select epost_tekst, min(tidspunkt) from varsel group by epost_tekst )
update epost_tekst et set first_seen_at = epost_tekst_seen.first_seen from epost_tekst_seen where et.id = epost_tekst_seen.id;

alter table varsel rename column eventtype to event_type;
