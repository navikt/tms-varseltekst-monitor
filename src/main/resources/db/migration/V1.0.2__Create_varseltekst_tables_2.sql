create table web_tekst (
    id serial primary key,
    tekst text unique
);

create table sms_tekst (
    id serial primary key,
    tekst text unique
);

create table epost_tittel (
    id serial primary key,
    tekst text unique
);

create table epost_tekst (
    id serial primary key,
    tekst text unique
);


create table varsel_v2 (
    id serial primary key,
    event_id text unique,
    eventType text unique,
    produsent_namespace text,
    produsent_appnavn text,
    sms_preferert boolean,
    epost_preferert boolean,
    varsel_tekst int references web_tekst(id),
    sms_tekst int references sms_tekst(id),
    epost_tittel int references epost_tittel(id),
    epost_tekst int references epost_tekst(id),
    varseltidspunkt timestamp,
    tidspunkt timestamp
)
