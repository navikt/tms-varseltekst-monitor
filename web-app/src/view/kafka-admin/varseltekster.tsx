import React, { useState } from 'react';
import { errorToast, successToast, warningToast } from '../../utils/toast-utils';
import { Card } from '../../component/card/card';
import {BodyShort, Button, Loader, Modal, RadioGroup, Select, TextField, Radio, DatePicker} from '@navikt/ds-react';
import {
	DownloadRequest, requestDownload,
} from '../../api';
import './kafka-admin.css';
import { toTimerStr } from '../../utils/date-utils';

export function Varseltekster() {

	return (
		<div className="view varseltekst-download">
			<div>
				<ReadFromTopicCard/>
			</div>
		</div>
	);
}

enum Teksttype {
	WEB_TEKST = 'WebTekst',
	SMS_TEKST = 'SmsTekst',
	EPOST_TITTEL = 'EpostTittel',
	EPOST_TEKST = 'EpostTekst'
}

enum Varseltype {
	ALLE ,
	BESKJED = 'Beskjed',
	OPPGAVE = 'Oppgave',
	INNBOKS = 'Innboks',
}


function ReadFromTopicCard() {
	const [isLoading, setIsLoading] = useState<boolean>(false);
	const [teksttypeField, setTeksttypeField] = useState<Teksttype>(Teksttype.WEB_TEKST);
	const [varseltypeField, setVarseltypeField] = useState<Varseltype>(Varseltype.ALLE);
	const [detaljertField, setDetaljertField] = useState<boolean>(false);
	const [fromDateField, setFromDateField] = useState<string>('');
	const [toDateField, setToDateField] = useState<string>('');

	async function handleDownload() {
		setIsLoading(true);

		let varseltype: string | null;

		if (varseltypeField === Varseltype.ALLE) {
			varseltype = null;
		} else {
			varseltype = varseltypeField
		}

		const request: DownloadRequest = {
			teksttype: teksttypeField,
			detaljert: detaljertField,
			varseltype: varseltype,
			startDato: fromDateField ? fromDateField : null,
			sluttDato: toDateField ? toDateField : null,
			inkluderStandardtekster: true,
			minimumAntall: 100,
			filnavn: null,
		};

		requestDownload(request)
			.catch(() => errorToast('Klarte ikke laste ned varseltekster'))
			.finally(() => {
				setIsLoading(false);
			});
	}

	// @ts-ignore
	return (
		<Card
			title="Statistikk for varseltekster"
			className="varseltekster-card very-large-card center-horizontal"
			innholdClassName="card__content"
		>
			<BodyShort spacing>
				Hent utrekk av hvilke varseltekster som sendes ut, og i hvilket antall
			</BodyShort>

			<Select
				label="Tekst-type"
				value={teksttypeField}
				onChange={e => setTeksttypeField(e.target.value as Teksttype)}
			>
				<option value={Teksttype.WEB_TEKST}>Web-tekst (Tekst på min side)</option>
				<option value={Teksttype.SMS_TEKST}>Sms-tekst</option>
				<option value={Teksttype.EPOST_TITTEL}>Epost-tittel</option>
				<option value={Teksttype.EPOST_TEKST}>Epost-tekst</option>
			</Select>

			<RadioGroup
				legend="Tell antall..."
				onChange={(value: boolean) => setDetaljertField(value)}
				defaultValue={false}
				required
			>
				<Radio value={false}>Totalt</Radio>
				<Radio value={true}>Fordelt på varseltype og produsent</Radio>
			</RadioGroup>

			<Select
				label="Varseltype"
				value={varseltypeField}
				onChange={e => setVarseltypeField(e.target.value as Varseltype || Varseltype.ALLE)}
			>
				<option value=''>Alle</option>
				<option value={Varseltype.BESKJED}>Beskjed</option>
				<option value={Varseltype.OPPGAVE}>Oppgave</option>
				<option value={Varseltype.INNBOKS}>Innboks</option>
			</Select>

			<DatePicker>
				<DatePicker.Input label="Fra og med" value={fromDateField} onChange={e => setFromDateField(e.target.value)}/>
				<DatePicker.Input label="Til" value={toDateField} onChange={e => setToDateField(e.target.value)}/>
			</DatePicker>

			{!isLoading ? (
				<Button id="fetch" onClick={handleDownload} variant="tertiary">
					Last ned
				</Button>
			) : null}

			{isLoading ? (
				<div className="read-from-topic-card__loader">
					<Loader size="2xlarge" />
				</div>
			) : null}
		</Card>
	);
}
