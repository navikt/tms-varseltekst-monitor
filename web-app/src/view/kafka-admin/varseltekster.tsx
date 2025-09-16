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
	DownloadRequest, sendVarselQuery,
} from '../../api';
import './kafka-admin.css';
import {f} from "msw/lib/glossary-de6278a9";
import {response} from "msw";

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

const MAX_INTERVAL_MS: number = 5000

const TEKSTTYPE_ERROR: string = "Du må velge minst én tekst-type"

function ReadFromTopicCard() {
	const [isLoading, setIsLoading] = useState<boolean>(false);
	const [teksttyperField, setTeksttyperField] = useState<string[]>([]);
	const [teksttyperError, setTeksttyperError] = useState<string>();
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

	const handleTekstType = (typer: string[]) => {
		if (typer.length == 0) {
			setTeksttyperError(TEKSTTYPE_ERROR)
		}
		setTeksttyperField(typer)
	}

	function awaitFile(fileLocation: string, nextInterval: number = 250) {
		setTimeout(() => {
			fetch(`${fileLocation}/status`)
				.then(response => response.text())
				.then(status => {
					if (status == "Pending") {
						awaitFile(fileLocation, Math.min(nextInterval * 2, MAX_INTERVAL_MS))
					} else if (status == "Complete") {
						window.open(fileLocation, '_self')
						setIsLoading(false)
					} else {
						setIsLoading(false)
					}
				})
		}, nextInterval)
	}

	async function handleDownloadQuery() {
		if (teksttyperField.length == 0) {
			setTeksttyperError(TEKSTTYPE_ERROR)
			return
		}

		setIsLoading(true);

		let varseltype: string | null;

		if (varseltypeField === Varseltype.ALLE) {
			varseltype = null;
		} else {
			varseltype = varseltypeField
		}

		const request: DownloadRequest = {
			teksttyper: teksttyperField,
			detaljert: detaljertField,
			varseltype: varseltype,
			startDato: fromDateField?.toISOString() || null,
			sluttDato: toDateField?.toISOString() || null,
			inkluderStandardtekster: standardteksterField,
			minimumAntall: parseInt(minAntallField, 10),
			filnavn: filenameField || null
		};

		sendVarselQuery(request)
			.then(response => {
				if (response.status == 202) {
					const fileLocation = response.headers.get('Location')!!
					awaitFile(fileLocation)
				} else {
					setIsLoading(false)
				}
			})
			.catch(() => {
				errorToast('Klarte ikke laste ned varseltekster')
				setIsLoading(false)
			})
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

			<CheckboxGroup
				legend="Tekst-typer"
				onChange={handleTekstType}
				error={teksttyperError}
			>
				<Checkbox value={Teksttype.WEB_TEKST}>Web-tekst (Tekst på min side)</Checkbox>
				<Checkbox value={Teksttype.SMS_TEKST}>Sms-tekst</Checkbox>
				<Checkbox value={Teksttype.EPOST_TITTEL}>Epost-tittel</Checkbox>
				<Checkbox value={Teksttype.EPOST_TEKST}>Epost-tekst</Checkbox>
			</CheckboxGroup>

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
				<Button id="fetch" onClick={handleDownloadQuery} variant="tertiary">
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
