import re
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

def index_vsm(processed_docs):
    """
    Melakukan indexing menggunakan TF-IDF untuk model VSM.
    Mengembalikan vectorizer dan matriks TF-IDF.
    """
    vectorizer = TfidfVectorizer()
    tfidf_matrix = vectorizer.fit_transform(processed_docs)
    print("Indexing VSM (TF-IDF) selesai.")
    return vectorizer, tfidf_matrix

def search_vsm(query, documents, filenames, vectorizer, tfidf_matrix, preprocess_func, stop_words_list, top_n=5):
    """
    Melakukan pencarian menggunakan model VSM.
    Mengembalikan daftar nama file yang relevan.
    """
    if vectorizer is None or tfidf_matrix is None:
        print("Model VSM (vectorizer atau tfidf_matrix) belum diinisialisasi dengan benar.")
        return []

    # Preprocess query untuk matching dan snippet
    # 'preprocess_func' dan 'stop_words_list' diterima sebagai argumen
    processed_query_text = preprocess_func(query, stop_words_list)
    effective_search_terms_for_snippet = set(processed_query_text.split())

    query_vec = vectorizer.transform([processed_query_text])
    similarities = cosine_similarity(query_vec, tfidf_matrix).flatten()
    
    # Filter hasil dengan skor > 0
    relevant_indices = [i for i, score in enumerate(similarities) if score > 0]
    sorted_relevant_indices = sorted(relevant_indices, key=lambda i: similarities[i], reverse=True)
    top_indices = sorted_relevant_indices[:top_n]

    retrieved_filenames_for_eval = []
    print(f"\n--- Hasil Pencarian VSM untuk query: \"{query}\" ---")
    if not top_indices:
        print("Tidak ada dokumen yang relevan ditemukan oleh VSM.")
        return []

    for i, idx in enumerate(top_indices):
        original_text = documents[idx]
        retrieved_filenames_for_eval.append(filenames[idx])

        sentences = re.split(r'(?<=[.!?])\s+', original_text)
        snippet = "[Cuplikan tidak ditemukan atau dokumen kosong.]"
        best_snippet_sentence = None

        for sentence_text in sentences:
            if not sentence_text.strip(): # Lewati kalimat kosong
                continue
            
            # Preprocess kalimat untuk matching snippet
            processed_sentence_text = preprocess_func(sentence_text, stop_words_list)
            sentence_terms_for_snippet = set(processed_sentence_text.split())
            
            if effective_search_terms_for_snippet.intersection(sentence_terms_for_snippet):
                best_snippet_sentence = sentence_text.strip()
                break # Ambil kalimat pertama yang cocok
        
        if best_snippet_sentence:
            snippet = best_snippet_sentence
        elif sentences and sentences[0].strip(): # Fallback ke kalimat pertama
            snippet = sentences[0].strip()

        print(f"Rank: {i+1}")
        print(f"Skor (Cosine Similarity): {similarities[idx]:.4f}")
        print(f"File: {filenames[idx]}")
        print(f"Cuplikan: {snippet}")
        print("-" * 80)
    return retrieved_filenames_for_eval