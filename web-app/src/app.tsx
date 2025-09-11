import React from 'react';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import './app.css';
import 'react-toastify/dist/ReactToastify.css';
import { Varseltekster } from './view/kafka-admin/varseltekster';
import { Header } from './component/header/header';
import Venterom from "./venterom";

function App() {
	return (
		<BrowserRouter>
			<Routes>
				<Route path="/" element={<Index />}></Route>
				<Route path="/venterom/:fileId" element={<Venterom />}></Route>
			</Routes>
		</BrowserRouter>
	);
}

function Index() {
	return (
		<div className="app tms-varseltekst-monitor">
			<Header/>
			<main>
				<Varseltekster/>
			</main>
			<ToastContainer/>
		</div>
	)
}

export default App;
