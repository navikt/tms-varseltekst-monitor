create table varseltekster_v1 (
    id serial primary key,
    produsent_namespace text,
    produsent_appnavn text,
    sms_preferert boolean,
    epost_preferert boolean,
    varsel_tekst,
    sms_tekst text,
    epost_tittel text,
    epost_tekst text,
    tidspunkt timestamp
)
