package com.language.detector.bean;

import java.util.List;

public class TranscriptionResponse {
    private String jobName;
    private String accountId;
    private String status;
    private Results results;
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Results getResults() {
		return results;
	}
	public void setResults(Results results) {
		this.results = results;
	}
	public TranscriptionResponse() {
		super();
		// TODO Auto-generated constructor stub
	}

    // Getters and Setters
    
}

class Results {
    private List<Transcript> transcripts;
    private List<Item> items;
    private List<AudioSegment> audio_segments;
	public List<Transcript> getTranscripts() {
		return transcripts;
	}
	public void setTranscripts(List<Transcript> transcripts) {
		this.transcripts = transcripts;
	}
	public List<Item> getItems() {
		return items;
	}
	public void setItems(List<Item> items) {
		this.items = items;
	}
	public List<AudioSegment> getAudio_segments() {
		return audio_segments;
	}
	public void setAudio_segments(List<AudioSegment> audio_segments) {
		this.audio_segments = audio_segments;
	}
	public Results() {
		super();
		// TODO Auto-generated constructor stub
	}

    // Getters and Setters
    
}

class Transcript {
    private String transcript;

	public String getTranscript() {
		return transcript;
	}

	public void setTranscript(String transcript) {
		this.transcript = transcript;
	}

    // Getters and Setters
    
}

class Item {
    private int id;
    private String type;
    private List<Alternative> alternatives;
    private String start_time;
    private String end_time;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<Alternative> getAlternatives() {
		return alternatives;
	}
	public void setAlternatives(List<Alternative> alternatives) {
		this.alternatives = alternatives;
	}
	public String getStart_time() {
		return start_time;
	}
	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}
	public String getEnd_time() {
		return end_time;
	}
	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}

    // Getters and Setters
    
}

class Alternative {
    private String confidence;
    private String content;
	public String getConfidence() {
		return confidence;
	}
	public void setConfidence(String confidence) {
		this.confidence = confidence;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Alternative() {
		super();
		// TODO Auto-generated constructor stub
	}

    // Getters and Setters
    
}

class AudioSegment {
    private int id;
    private String transcript;
    private String start_time;
    private String end_time;
    private List<Integer> items;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTranscript() {
		return transcript;
	}
	public void setTranscript(String transcript) {
		this.transcript = transcript;
	}
	public String getStart_time() {
		return start_time;
	}
	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}
	public String getEnd_time() {
		return end_time;
	}
	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}
	public List<Integer> getItems() {
		return items;
	}
	public void setItems(List<Integer> items) {
		this.items = items;
	}
	public AudioSegment() {
		super();
		// TODO Auto-generated constructor stub
	}

    // Getters and Setters
    
}


