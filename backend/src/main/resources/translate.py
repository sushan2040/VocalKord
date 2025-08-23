import sys
from transformers import MarianMTModel, MarianTokenizer

def translate(text, src_lang, tgt_lang):
    model_name = f"Helsinki-NLP/opus-mt-{src_lang}-{tgt_lang}"
    tokenizer = MarianTokenizer.from_pretrained(model_name)
    model = MarianMTModel.from_pretrained(model_name)

    inputs = tokenizer([text], return_tensors="pt", padding=True)
    translated = model.generate(**inputs)
    result = tokenizer.decode(translated[0], skip_special_tokens=True)
    return result

if __name__ == "__main__":
    if len(sys.argv) < 4:
        print("Usage: python translate.py <src_lang> <tgt_lang> <text>")
        sys.exit(1)

    src_lang = sys.argv[1]
    tgt_lang = sys.argv[2]
    text = sys.argv[3]

    translation = translate(text, src_lang, tgt_lang)
    print(translation)
