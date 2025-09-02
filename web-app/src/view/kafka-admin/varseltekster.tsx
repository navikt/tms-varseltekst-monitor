import React, { useState } from 'react';
import { errorToast, successToast, warningToast } from '../../utils/toast-utils';
import { Card } from '../../component/card/card';
import {
	BodyShort,
	Button,
	Loader,
	Modal,
	RadioGroup,
	Select,
	Radio,
	DatePicker,
	TextField,
	useDatepicker,
	Checkbox,
	CheckboxGroup,
    Switch
} from '@navikt/ds-react';
import {
	DownloadRequest, requestDownload,
} from '../../api';
import './kafka-admin.css';
import { toTimerStr } from '../../utils/date-utils';
import {response} from "msw";
import {number} from "prop-types";

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
	const [fromDateField, setFromDateField] = useState<Date | null>(null);
	const [toDateField, setToDateField] = useState<Date | null>(null);
	const [minAntallField, setMinAntallField] = useState<string>('');
	const [filenameField, setFilenameField] = useState<string>('');
	const [standardteksterField, setStandardteksterField] = useState<boolean>(false);

	const fromDatePicker = useDatepicker({
		onDateChange: (date) => setFromDateField(date || null),
	})

	const toDatePicker = useDatepicker({
		onDateChange: (date) => setToDateField(date || null),
	})

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
			startDato: fromDateField?.toISOString() || null,
			sluttDato: toDateField?.toISOString() || null,
			inkluderStandardtekster: standardteksterField,
			minimumAntall: parseInt(minAntallField, 10),
			filnavn: filenameField || null,
		};

		requestDownload(request)
			.then(response => response.headers.get('Location')!!)
			.then(fileLink => window.open(fileLink, '_self'))
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

			<Switch checked={standardteksterField} onChange={(e) => setStandardteksterField(e.target.checked)}>
				Inkluder standardtekster
			</Switch>

			<DatePicker {...fromDatePicker.datepickerProps}>
				<DatePicker.Input {...fromDatePicker.inputProps} label="Fra og med" />
			</DatePicker>

			<DatePicker {...toDatePicker.datepickerProps}>
				<DatePicker.Input {...toDatePicker.inputProps} label="Til"/>
			</DatePicker>

			<TextField
				label="Minimum antall for visning av tekst (min 100)"
				value={minAntallField}
				inputMode="numeric"
				onChange={e => setMinAntallField(e.target.value)}
			/>

			<TextField
				label="Filnavn (.xlsx)"
				value={filenameField}
				onChange={e => setFilenameField(e.target.value)}
			/>

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
