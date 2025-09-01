import React from 'react';
import {useParams} from 'react-router';
import { ToastContainer } from 'react-toastify';
import './app.css';
import 'react-toastify/dist/ReactToastify.css';
import { Varseltekster } from './view/kafka-admin/varseltekster';
import { Header } from './component/header/header';

function App() {
    return (
        <div className="venterom">
            <main>
                <Statusboks />
            </main>
        </div>
    );
}

function Statusboks() {

    const fileId = useParams().fileId

    return (
        <div>
            <div>
                <ReadFromTopicCard/>
            </div>
        </div>
    );
}

export default App;
