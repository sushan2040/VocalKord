import React, { useEffect, useRef, useState } from 'react';
import './App.css';
import Sidebar from './Sidebar';
import axios from 'axios';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import TranslateText from './Components/TranslateTexts';
import TranslateDocuments from './Components/TranslateDocuments';
import SpeechToText from './Components/SpeechToText';

function App() {
  return (
<>
<BrowserRouter>
          <Routes>
            <Route path="/" element={<TranslateText />} />
            <Route path="/translate-documents" element={<TranslateDocuments />} />
            <Route path="/speech-to-text" element={<SpeechToText/>} />
          </Routes>
        </BrowserRouter>
</>
  );
}

export default App;