import React from 'react';
import { ToastContainer } from 'react-toastify';
import './app.css';
import 'react-toastify/dist/ReactToastify.css';
import { Varseltekster } from './view/kafka-admin/varseltekster';
import { Header } from './component/header/header';

function App() {
	return (
		<div className="app tms-varseltekst-monitor">
			<Header />
			<main>
				<Varseltekster />
			</main>
			<ToastContainer />
		</div>
	);
}

export default App;
