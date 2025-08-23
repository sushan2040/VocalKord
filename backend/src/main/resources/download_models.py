from transformers import MarianMTModel, MarianTokenizer
import itertools

languages = [
    "af", "am", "ar", "az", "be", "bg", "bn", "bs", "ca", "cs", "cy", "da", "de",
    "el", "en", "es", "et", "fa", "fi", "fr", "gu", "he", "hi", "hr", "hu", "hy",
    "id", "is", "it", "ja", "jv", "ka", "kk", "km", "kn", "ko", "lt", "lv", "ml",
    "mn", "mr", "ms", "my", "ne", "nl", "no", "pa", "pl", "pt", "ro", "ru", "si",
    "sk", "sl", "sq", "sr", "sv", "sw", "ta", "te", "th", "tl", "tr", "uk", "ur",
    "vi", "zh"
]

# All language combinations except same-language
language_pairs = [
    (src, tgt) for src, tgt in itertools.product(languages, languages) if src != tgt
]

print("Starting model downloads...")
downloaded = 0
skipped = 0

for src, tgt in language_pairs:
    model_name = f"Helsinki-NLP/opus-mt-{src}-{tgt}"
    try:
        print(f"🔽 Downloading: {model_name}")
        MarianTokenizer.from_pretrained(model_name)
        MarianMTModel.from_pretrained(model_name)
        downloaded += 1
    except Exception as e:
        print(f"⏭️ Skipping {model_name} (not found or failed): {e}")
        skipped += 1

print(f"\n✅ Total Downloaded: {downloaded}")
print(f"⏭️ Total Skipped: {skipped}")
