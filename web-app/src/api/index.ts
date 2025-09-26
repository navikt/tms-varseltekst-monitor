import axios, { AxiosPromise } from 'axios';

export const axiosInstance = axios.create({
	withCredentials: true
});

export interface DownloadRequest {
	teksttyper: string[],
	detaljert: boolean,
	varseltype: string | null,
	startDato: string | null,
	sluttDato: string | null,
	harEksternVarsling: boolean | null,
	inkluderStandardtekster: boolean,
	inkluderUbrukteKanaler: boolean,
	minimumAntall: number,
	filnavn: string | null
}

export function sendVarselQuery(request: DownloadRequest): Promise<Response> {
	return fetch(`/api/download`, {
		method: 'POST',
		body: JSON.stringify({
			teksttyper: request.teksttyper,
			detaljert: request.detaljert,
			varseltype: request.varseltype,
			startDato: request.startDato,
			sluttDato: request.sluttDato,
			harEksternVarsling: request.harEksternVarsling,
			inkluderStandardtekster: request.inkluderStandardtekster,
			inkluderUbrukteKanaler: request.inkluderUbrukteKanaler,
			minimumAntall: request.minimumAntall,
			filnavn: request.filnavn
		}),
		headers: {
			'Content-type': 'application/json; charset=UTF-8'
		}
	});
}
