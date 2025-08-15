import axios from "axios";
import { useEffect, useRef, useState } from "react";
import Sidebar from "../Sidebar";
import { toast, ToastContainer } from "react-toastify";

export default function TranslateText() {
  const appURL = process.env.REACT_APP_API_URL;
  const toBeTranslateText = useRef("");
  const [translatedText, setTranslatedText] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [languages, setLanguages] = useState([]);
  const [errors, setErrors] = useState({});
  const [fromLanguage, setFromLanguage] = useState("");
  const [toLanguage, setToLanguage] = useState("");

  var validattionErrors = {
    fromLanguageError: "",
    toLanguageError: "",
    text: ""
  }

  function translateText() {
    if (fromLanguage == "") {
      validattionErrors.fromLanguageError = "Please select";
      setErrors(validattionErrors);
      toast.error("Please select from language!");
      return;
    } else {
      validattionErrors.fromLanguageError = null;
      setErrors(validattionErrors);
    }
    if (toLanguage == "") {
      validattionErrors.toLanguageError = "Please select";
      setErrors(validattionErrors);
      toast.error("Please select to language!");
      return;
    } else {
      validattionErrors.toLanguageError = null;
      setErrors(validattionErrors);
    }
    if (toBeTranslateText.current.value == "") {
      validattionErrors.text = "This field is required!";
      setErrors(validattionErrors);
      toast.error("Please write something in the text box!");
      return;
    } else {
      validattionErrors.text = null;
      setErrors(validattionErrors);
    }

    setIsLoading(true);
    var text = toBeTranslateText.current.value;
    axios.post(appURL + "/api/translate", {}, {
      params: {
        text: text,
        fromLanguage: fromLanguage,
        toLanguage: toLanguage
      },
    }).then((result) => {
      setIsLoading(false);
      console.log(result.data);
      setTranslatedText(result.data);
    }).catch((e) => {
      toast.error(e.response.data);
    })
  }
  function getLanguages() {
    axios.get(appURL + "/api/get-languages")
      .then((result) => {
        setLanguages(result.data);
      })
  }
  function downloadModels() {
    axios.get(appURL + "/api/download-models");
  }
  useEffect(() => {
    getLanguages();
  })
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
                  {errors.fromLanguageError && <span className="error">{errors.fromLanguageError}</span>}
                </div>
                <textarea style={{ width: '100%' }} rows="15" ref={toBeTranslateText} placeholder='Add text for translation'></textarea>
                {errors.text && <span className="error">{errors.text}</span>}
              </div>
              <div className='col-sm-2 mb-2 mt-2'>

                {!isLoading ? <a className='btn btn-primary' onClick={translateText}>Exchange</a>
                  : <button className="btn btn-primary" type="button" disabled>
                    <span className="spinner-grow spinner-grow-sm" role="status" aria-hidden="true"></span>
                    Loading...
                  </button>}
              </div>
              <div className='col-sm-5 mb-2 mt-2 shadow-lg p-3 mb-5 bg-white rounded' >
                <div className=''>
                  <label>To Language</label>
                  <div className=''>
                    <select className='form-control mt-2 mb-2' value={toLanguage} onChange={(e) => setToLanguage(e.target.value)}>
                      <option value="0">--Please select--</option>
                      {Object.entries(languages).map(([code, name]) => (
                        <option key={code} value={code}>
                          {name}
                        </option>
                      ))}
                    </select>
                  </div>
                  {errors.toLanguageError && <span className="error">{errors.toLanguageError}</span>}

                </div>
                <textarea style={{ width: '100%' }} rows="15" placeholder='Translated text' value={translatedText}></textarea>
              </div>
            </div>
          </div>
        </div>
      </div>
      <ToastContainer />
    </div>
  );
}