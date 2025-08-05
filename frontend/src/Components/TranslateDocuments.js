import React, { useEffect, useRef, useState } from 'react';
import '../App.css';
import Sidebar from '../Sidebar';
import axios from 'axios';

function TranslateDocuments() {
  const appURL="http://localhost:8080";
  const toBeTranslateText=useRef("");
  const [translatedText,setTranslatedText]=useState("");
  const [isLoading,setIsLoading]=useState(false);
  const [languages,setLanguages]=useState([]);
  const [fromLanguage,setFromLanguage]=useState("");
  const [toLanguage,setToLanguage]=useState("");
  const [fileToBeTranslated,setFileToBeTranslated]=useState(null);
    function translateText(){
    setIsLoading(true);
    var text=toBeTranslateText.current.value;
    axios.post(appURL+"/api/translate",{},{
      params:{
        text:text,
        fromLanguage:fromLanguage,
        toLanguage:toLanguage
      },
    }).then((result)=>{
      setIsLoading(false);
      console.log(result.data);
      setTranslatedText(result.data);
    }).catch((e)=>{
      console.log(e);
    })
  }
  function getLanguages(){
    axios.get(appURL+"/api/get-languages")
    .then((result)=>{
     setLanguages(result.data);
    })
  }
  function downloadModels(){
    axios.get(appURL+"/api/download-models");
  }
   useEffect(()=>{
    getLanguages();
  })
  function translateDocuments() {
  const formData = new FormData();
  formData.append('file', fileToBeTranslated);
  formData.append('fromLanguage', fromLanguage);
  formData.append('toLanguage', toLanguage);

  axios.post(appURL + "/api/translate-document", formData, {
    responseType: 'blob', // 👈 Important for file download
    headers: {
      "Content-Type": 'multipart/form-data',
    }
  }).then((response) => {
    console.log(response);
   const mimeType = response.headers['content-type'];

let filename = getFilenameFromDisposition(response.headers['content-disposition']);
if (!filename) {
  const extMap = {
    'application/pdf': 'pdf',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'docx',
    'text/plain': 'txt'
  };
  const ext = extMap[mimeType] || 'txt';
  filename = `translated.${ext}`;
}
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename); 
    document.body.appendChild(link);
    link.click();
    link.remove();
  }).catch((error) => {
    console.log("Error:", error);
  });
}

// Helper to extract filename from headers
function getFilenameFromDisposition(disposition) {
  if (!disposition) return null;
  const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
  const matches = filenameRegex.exec(disposition);
  return matches != null ? matches[1].replace(/['"]/g, '') : null;
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
            <div className='row' style={{alignItems:'center'}}>
              <div className='col-sm-5 mb-2 mt-2 shadow-lg p-3 mb-5 bg-white rounded'>
               <div className=''>
                <label>From Language</label>
                 <div className=''>
               <select className='form-control mt-2 mb-2' value={fromLanguage} onChange={(e)=>setFromLanguage(e.target.value)}>
                <option value="0">--Please select--</option>
                {Object.entries(languages).map(([code, name]) => (
    <option key={code} value={code}>
      {name}
    </option>
  ))}
               </select>
              
               </div>
               <div className='mb-2 mt-2'>
                    <label>Upload Documents</label>
                    <div className=''>
                         <input onChange={(e)=>setFileToBeTranslated(e.target.files[0])} type='file' placeholder='Upload file'/>
                    </div>
               </div>
              </div> 
                </div>
              <div className='col-sm-2 mb-2 mt-2'>
                {!isLoading ? <a className='btn btn-primary' onClick={translateDocuments}>Translate and download document</a>
                :<button className="btn btn-primary" type="button" disabled>
  <span className="spinner-grow spinner-grow-sm" role="status" aria-hidden="true"></span>
  Loading...
</button>}
              </div>
            <div className='col-sm-5 mb-2 mt-2 shadow-lg p-3 mb-5 bg-white rounded' >
              <div className=''>
                <label>To Language</label>
                <div className=''>
                <select className='form-control mt-2 mb-2' value={toLanguage} onChange={(e)=>setToLanguage(e.target.value)}>
                <option value="0">--Please select--</option>
                {Object.entries(languages).map(([code, name]) => (
    <option key={code} value={code}>
      {name}
    </option>
  ))}
               </select>
               </div>
              
               </div>
               </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default TranslateDocuments;