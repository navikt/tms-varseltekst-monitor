import React from 'react';
import { createRoot } from 'react-dom/client';
import App from './app';
import * as dayjs from 'dayjs';
import 'dayjs/locale/nb';

dayjs.locale('nb');

const domNode = document.getElementById('root');
// @ts-ignore
const reactRoot = createRoot(domNode);
reactRoot.render(<App />);

