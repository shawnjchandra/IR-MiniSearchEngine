import os
import string
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from nltk.corpus import stopwords
import nltk
import re

# Download stopwords jika belum tersedia
# nltk.download('stopwords')

# Lokasi folder dataset
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATASET_FOLDER = os.path.join(BASE_DIR, "datasets")

# Ambil daftar file dalam folder datasets
def load_documents_from_folder(folder_path):
    documents = []
    filenames = []
    for filename in sorted(os.listdir(folder_path)):
        file_path = os.path.join(folder_path, filename)
        if filename.endswith(".txt"):
            with open(file_path, "r", encoding="utf-8") as f:
                documents.append(f.read())
                filenames.append(filename)
    return documents, filenames

# Stopwords bahasa Indonesia
stop_words = set(stopwords.words('indonesian'))

# Fungsi preprocessing
def preprocess(text):
    text = text.lower()
    tokens = text.split()
    tokens = [word.strip(string.punctuation) for word in tokens]
    tokens = [word for word in tokens if word and word not in stop_words]
    return ' '.join(tokens)

# Muat dan proses dokumen dari folder
documents, filenames = load_documents_from_folder(DATASET_FOLDER)
processed_docs = [preprocess(doc) for doc in documents]

# TF-IDF Vectorization
vectorizer = TfidfVectorizer()
tfidf_matrix = vectorizer.fit_transform(processed_docs)

# Fungsi pencarian
def search(query, top_n=5):
    query_terms = set(query.lower().split())  # Ambil kata-kata dari query asli

    processed_query = preprocess(query)
    query_vec = vectorizer.transform([processed_query])
    similarities = cosine_similarity(query_vec, tfidf_matrix).flatten()
    top_indices = similarities.argsort()[::-1][:top_n]

    print(f"\nHasil pencarian untuk query: \"{query}\"\n")
    for idx in top_indices:
        original_text = documents[idx]

        # Bagi dokumen jadi kalimat
        sentences = re.split(r'(?<=[.!?]) +', original_text)

        # Cari kalimat yang mengandung kata dari query (cocokkan kata apa adanya)
        snippet = None
        for sentence in sentences:
            sentence_words = set(sentence.lower().split())
            if query_terms & sentence_words:  # ada irisan
                snippet = sentence.strip()
                break  # ambil satu kalimat saja

        if snippet is None:
            snippet = "[Tidak ditemukan kalimat yang cocok dengan query.]"

        print(f"Skor: {similarities[idx]:.4f}")
        print(f"File: {filenames[idx]}")
        print(f"Cuplikan: {snippet}")
        print("-" * 80)

if __name__ == "__main__":
    while True:
        query = input("Masukkan query pencarian (atau ketik 'exit' untuk keluar): ")
        if query.lower() == 'exit':
            print("Terima kasih! Program selesai.")
            break
        search(query)
