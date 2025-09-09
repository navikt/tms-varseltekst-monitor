import axios, { AxiosPromise } from 'axios';

export const axiosInstance = axios.create({
	withCredentials: true
});

export interface DownloadRequest {
	teksttype: string,
	detaljert: boolean,
	varseltype: string | null,
	startDato: string | null,
	sluttDato: string | null,
	inkluderStandardtekster: boolean,
	minimumAntall: number,
	filnavn: string | null
}

export function requestDownload(request: DownloadRequest): Promise<Response> {
	return fetch(`/api/download`, {
		method: 'POST',
		body: JSON.stringify({
			teksttype: request.teksttype,
			detaljert: request.detaljert,
			varseltype: request.varseltype,
			startDato: request.startDato,
			sluttDato: request.sluttDato,
			inkluderStandardtekster: request.inkluderStandardtekster,
			minimumAntall: request.minimumAntall,
			filnavn: request.filnavn
		}),
		headers: {
			'Content-type': 'application/json; charset=UTF-8'
		}
	});
}
