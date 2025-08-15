import React, { useEffect, useRef, useState } from 'react';
import '../App.css';
import Sidebar from '../Sidebar';
import axios from 'axios';
import { toast, ToastContainer } from "react-toastify";

export default function SpeechToText() {
  const appURL = process.env.REACT_APP_API_URL;
  const toBeTranslateText = useRef("");
  const [translatedText, setTranslatedText] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [languages, setLanguages] = useState([]);
  const [fromLanguage, setFromLanguage] = useState("");
  const [errors, setErrors] = useState({});
  const [toLanguage, setToLanguage] = useState("");
  const [fileToBeTranslated, setFileToBeTranslated] = useState(null);


  const [recording, setRecording] = useState(false);
  const [audioURL, setAudioURL] = useState(null);
  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);
  var validattionErrors = {
    fromLanguageError: "",
  }

  const startRecording = async () => {
    if (fromLanguage == "") {
      validattionErrors.fromLanguageError = "Please select";
      setErrors(validattionErrors);
      toast.error("Please select from langauge!");
      return;
    } else {
      validattionErrors.fromLanguageError = null;
      setErrors(validattionErrors);
    }
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    mediaRecorderRef.current = new MediaRecorder(stream);
    mediaRecorderRef.current.ondataavailable = e => audioChunksRef.current.push(e.data);
    mediaRecorderRef.current.onstop = async () => {
      const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/webm' });
      audioChunksRef.current = [];
      const audioUrl = URL.createObjectURL(audioBlob);
      setAudioURL(audioUrl);



      // Send audioBlob to backend
      const formData = new FormData();
      formData.append('file', audioBlob, 'recording.webm');
      formData.append('fromLanguage', fromLanguage);
      axios.post(appURL + '/api/transcribe', formData, {
        headers: {
          "Content-Type": 'multipart/form-data',
        }
      }).then((result) => {
        console.log(result.data.results.transcripts[0].transcript);
        setTranslatedText(result.data.results.transcripts[0].transcript);
      }).then((e) => {
        toast.error(e.response.data);
      });
    };
    mediaRecorderRef.current.start();
    setRecording(true);
  };

  const stopRecording = () => {
    mediaRecorderRef.current.stop();
    setRecording(false);
  };

  useEffect(() => {
    getLanguages();
  }, [])
  function getLanguages() {
    axios.get(appURL + "/api/get-languages")
      .then((result) => {
        setLanguages(result.data);
      })
  }
  return (
    <div className="App" id="outer-container">
      <Sidebar pageWrapId={'page-wrap'} outerContainerId={'outer-container'} />
      <div id="page-wrap">
        <h1>Vocal Kord</h1>
        <h2>Translate to other languages at ease!</h2>
      </div>
      <div className='row'>
        <div className='col-lg-12'>
          <div className='card card-body'>
            <div className='row' style={{ alignItems: 'center' }}>
              <div className='col-sm-5 mb-2 mt-2 shadow-lg p-3 mb-5 bg-white rounded'>
                <div className=''>
                  <label>From Language</label>
                  <div className=''>
                    <select className='form-control mt-2 mb-2' value={fromLanguage} onChange={(e) => setFromLanguage(e.target.value)}>
                      <option value="0">--Please select--</option>
                      {Object.entries(languages).map(([code, name]) => (
                        <option key={code} value={code}>
                          {name}
                        </option>
                      ))}
                    </select>
                  </div>
                  {errors.fromLanguageError && <span className='error'>{errors.fromLanguageError}</span>}
                  <div>
                    <button className='btn btn-danger' onClick={recording ? stopRecording : startRecording}>
                      {recording ? 'Stop' : 'Start'} Recording
                    </button>
                    {audioURL && <audio controls src={audioURL} />}
                  </div>
                  <div className='mb-2 mt-2'>
                    <label>Upload Documents</label>
                    <div className=''>
                      <input onChange={(e) => setFileToBeTranslated(e.target.files[0])} type='file' placeholder='Upload file' />
                    </div>
                  </div>
                </div>
              </div>
              <div className='col-sm-2 mb-2 mt-2'>

              </div>
              <div className='col-sm-5 mb-2 mt-2 shadow-lg p-3 mb-5 bg-white rounded' >
                <div className=''>

                  <textarea style={{ width: '100%' }} rows="10" placeholder='Translated text' value={translatedText}></textarea>

                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <ToastContainer />
    </div>
  );
}