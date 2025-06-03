import re
from rank_bm25 import BM25Okapi # Impor library BM25

def index_bm25(processed_docs):
    """
    Melakukan indexing untuk model BM25.
    Mengembalikan model BM25 dan tokenized corpus.
    """
    print("Proses indexing BM25...")
    tokenized_corpus = [doc.split(" ") for doc in processed_docs]

    # Pengecekan dasar untuk tokenized_corpus
    if not tokenized_corpus:
        print("Peringatan: Korpus token kosong. BM25 tidak dapat diinisialisasi.")
        return None, None
    
    if not any(doc_tokens for doc_tokens in tokenized_corpus if any(doc_tokens)): 
        print("Peringatan: Semua dokumen dalam korpus kosong setelah tokenisasi atau hanya berisi token kosong. BM25 mungkin tidak berfungsi dengan baik.")

    valid_tokenized_corpus = [doc_tokens for doc_tokens in tokenized_corpus if doc_tokens] 
    
    if not valid_tokenized_corpus and any(processed_docs):
         print("Peringatan: Semua dokumen menjadi list token kosong setelah di-split. BM25 mungkin tidak efektif.")
    
    if not tokenized_corpus and not any(processed_docs):
        print("Peringatan: Korpus proses kosong, BM25 tidak diinisialisasi.")
        return None, None

    try:
        bm25_model = BM25Okapi(tokenized_corpus) # Gunakan tokenized_corpus asli
        print("Indexing BM25 selesai.")
        return bm25_model, tokenized_corpus
    except Exception as e:
        print(f"Gagal melakukan indexing BM25: {e}")
        print("Pastikan korpus token tidak sepenuhnya kosong dari terms yang valid.")
        return None, None

def search_bm25(query, documents, filenames, bm25_model, tokenized_corpus, preprocess_func, stop_words_list, top_n=5):
    """
    Melakukan pencarian menggunakan model BM25.
    Mengembalikan daftar nama file yang relevan.
    """
    if bm25_model is None: # tokenized_corpus tidak digunakan langsung oleh search jika bm25_model sudah ada
        print(f"\n--- Hasil Pencarian BM25 untuk query: \"{query}\" ---")
        print("Model BM25 belum diinisialisasi dengan benar atau indexing gagal.")
        return []

    print(f"\n--- Hasil Pencarian BM25 untuk query: \"{query}\" ---")

    # Preprocess query
    processed_query_text = preprocess_func(query, stop_words_list)
    effective_search_terms_for_snippet = set(processed_query_text.split())
    tokenized_query = processed_query_text.split(" ")
    
    # Jika query menjadi kosong setelah preprocessing
    if not tokenized_query or not any(tokenized_query):
        print("Query menjadi kosong setelah preprocessing. Tidak ada hasil dari BM25.")
        return []

    # Dapatkan skor dari BM25
    try:
        doc_scores = bm25_model.get_scores(tokenized_query)
    except Exception as e:
        print(f"Error saat mendapatkan skor BM25: {e}")
        return []
    
    relevant_indices = [i for i, score in enumerate(doc_scores) if score > 0]
    sorted_relevant_indices = sorted(relevant_indices, key=lambda i: doc_scores[i], reverse=True)
    top_indices = sorted_relevant_indices[:top_n]

    retrieved_filenames_for_eval_bm25 = []
    if not top_indices:
        print("Tidak ada dokumen yang relevan ditemukan oleh BM25 (skor > 0).")
        return []

    for i, idx in enumerate(top_indices):
        original_text = documents[idx]
        retrieved_filenames_for_eval_bm25.append(filenames[idx])

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
        print(f"Skor (BM25): {doc_scores[idx]:.4f}")
        print(f"File: {filenames[idx]}")
        print(f"Cuplikan: {snippet}")
        print("-" * 80)
    return retrieved_filenames_for_eval_bm25