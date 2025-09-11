import React, {useEffect, useRef, useState} from 'react';
import {useParams} from 'react-router';
import { ToastContainer } from 'react-toastify';
import './app.css';
import 'react-toastify/dist/ReactToastify.css';
import { Varseltekster } from './view/kafka-admin/varseltekster';
import { Header } from './component/header/header';
import {BodyShort, Loader} from "@navikt/ds-react";
import { CheckmarkCircleIcon, XMarkOctagonIcon } from '@navikt/aksel-icons';

function Venterom() {
    return (
        <div className="venterom">
            <main>
                <Statusboks />
            </main>
        </div>
    );
}

enum DownloadStatus {
    PENDING, COMPLETE, FAILED
}

function Statusboks() {

    const fileId = useParams().fileId

    const [status, setStatus] = useState<DownloadStatus>(DownloadStatus.PENDING);

    function fetchAPIData(){

        fetch(`/api/download/${fileId}`, { method: 'HEAD' })
            .then(response => {
                if(response.status == 102) {
                    setTimeout(fetchAPIData, 3000);
                } else if (response.status == 200) {
                    window.open(`/api/download/${fileId}`, '_self')
                    setStatus(DownloadStatus.COMPLETE)
                    setTimeout(_ => { close() } , 3000);
                } else {
                    setStatus(DownloadStatus.FAILED)
                    setTimeout(_ => { close() } , 3000);
                }
            })
    }

    return (
        <div>
            <div>
                <BodyShort spacing>
                    Klargjør uttrekk...
                </BodyShort>

                {status == DownloadStatus.PENDING ? (
                    <div className="read-from-topic-card__loader">
                        <Loader size="2xlarge" />
                    </div>
                ) : null}

                {status == DownloadStatus.COMPLETE ? (
                    <div>
                        <CheckmarkCircleIcon fontSize="3rem" />
                    </div>
                ) : null}

                {status == DownloadStatus.FAILED ? (
                    <div>
                        <XMarkOctagonIcon fontSize="3rem" />
                    </div>
                ) : null}
            </div>
        </div>
    );
}

export default Venterom;
