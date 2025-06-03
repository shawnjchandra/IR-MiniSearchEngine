import os
import re
from nltk.corpus import stopwords
# import nltk # Jika ingin menggunakan nltk.download() secara eksplisit

# Impor fungsi dari file model Anda
from vsm_model import index_vsm, search_vsm
from bm25_model import index_bm25, search_bm25

# Lokasi folder dataset
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATASET_FOLDER = os.path.join(BASE_DIR, "datasets") 

# --- Fungsi Utilitas ---
def load_documents_from_folder(folder_path):
    docs = []
    f_names = []
    if not os.path.exists(folder_path):
        print(f"Error: Folder dataset '{folder_path}' tidak ditemukan.")
        return docs, f_names
    for filename in sorted(os.listdir(folder_path)):
        file_path = os.path.join(folder_path, filename)
        if filename.endswith(".txt"):
            try:
                with open(file_path, "r", encoding="utf-8") as f:
                    docs.append(f.read())
                    f_names.append(filename)
            except Exception as e:
                print(f"Gagal membaca file {filename}: {e}")
    return docs, f_names

try:
    stop_words_indo = set(stopwords.words('indonesian'))
except LookupError:
    print("Stopwords NLTK untuk 'indonesian' belum diunduh.")
    print("Silakan unduh dengan menjalankan Python interpreter dan ketik:")
    print("import nltk")
    print("nltk.download('stopwords')")
    stop_words_indo = set()

def preprocess(text, stop_words_list):
    text = text.lower()
    tokens = re.findall(r'\b\w+\b', text) 
    tokens = [word for word in tokens if word and word not in stop_words_list and not word.isdigit()]
    return ' '.join(tokens)

def calculate_metrics(retrieved_filenames, relevant_filenames_gt):
    retrieved_set = set(retrieved_filenames)
    relevant_set_gt = set(relevant_filenames_gt)

    true_positives = len(retrieved_set.intersection(relevant_set_gt))
    false_positives = len(retrieved_set.difference(relevant_set_gt))
    false_negatives = len(relevant_set_gt.difference(retrieved_set))

    precision = true_positives / (true_positives + false_positives) if (true_positives + false_positives) > 0 else 0.0
    recall = true_positives / (true_positives + false_negatives) if (true_positives + false_negatives) > 0 else 0.0
    f1_score = 2 * (precision * recall) / (precision + recall) if (precision + recall) > 0 else 0.0

    return precision, recall, f1_score

if __name__ == "__main__":
    documents, filenames = load_documents_from_folder(DATASET_FOLDER)
    if not documents:
        print(f"Tidak ada dokumen yang dimuat dari {DATASET_FOLDER}. Program akan berhenti.")
        exit()
    
    print(f"Berhasil memuat {len(documents)} dokumen.")
    
    processed_docs = [preprocess(doc, stop_words_indo) for doc in documents]

    vsm_vectorizer, vsm_tfidf_matrix = index_vsm(processed_docs)
    bm25_model_obj, tokenized_corpus_bm25 = index_bm25(processed_docs)

    # --- Ground Truth ---
    ground_truth = {
        "teknologi pertanian modern": ["doc1.txt", "doc2.txt", "doc6.txt"],
        "penggunaan drone untuk petani": ["doc2.txt"],
        "ekonomi digital pedesaan": ["doc3.txt", "doc8.txt"],
        "banjir jawa tengah dan timur": ["doc4.txt"],
        "pertanian organik indonesia": ["doc5.txt"],
        "ai prediksi cuaca pertanian": ["doc6.txt"],
        "pendidikan di daerah terpencil": ["doc7.txt"],
        "penjualan hasil tani online": ["doc3.txt", "doc8.txt"],
        "transportasi listrik perkotaan": ["doc9.txt"],
        "ketahanan pangan dan pertanian vertikal": ["doc10.txt"],
        "subsidi pemerintah untuk petani": ["doc1.txt"],
        "modernisasi alat pertanian": ["doc1.txt"],
        "pemasaran digital petani": ["doc8.txt", "doc3.txt"],
        "peningkatan hasil panen": ["doc1.txt", "doc2.txt"],
        "cuaca ekstrem dan dampaknya": ["doc4.txt", "doc6.txt"]
    }
    print(f"\nGround truth berisi {len(ground_truth)} query.")
    print("Pastikan nama file di ground_truth sesuai dengan file di folder dataset Anda.")
    print("Query dalam ground_truth diasumsikan sudah dalam lowercase.")
    print("PERHATIAN: Ground truth di atas adalah CONTOH, harap sesuaikan dengan analisis relevansi Anda!")

    while True:
        query_input = input("\nMasukkan query pencarian (atau ketik 'exit' untuk keluar): ").strip()
        if query_input.lower() == 'exit':
            print("Terima kasih! Program selesai.")
            break
        if not query_input:
            print("Query tidak boleh kosong.")
            continue

        print("\n================ HASIL PENCARIAN ================")
        retrieved_docs_vsm = search_vsm(query_input, documents, filenames, 
                                        vsm_vectorizer, vsm_tfidf_matrix, 
                                        preprocess, stop_words_indo)
        
        retrieved_docs_bm25 = search_bm25(query_input, documents, filenames, 
                                          bm25_model_obj, tokenized_corpus_bm25, 
                                          preprocess, stop_words_indo)

        print("\n================ EVALUASI MODEL ================")
        query_key_for_gt = query_input.lower() 
        
        # Default F1 scores for this query
        f1_vsm, p_vsm, r_vsm = 0.0, 0.0, 0.0
        f1_bm25, p_bm25, r_bm25 = 0.0, 0.0, 0.0
        vsm_has_results = bool(retrieved_docs_vsm)
        bm25_has_results = bool(retrieved_docs_bm25)


        if query_key_for_gt in ground_truth:
            relevant_docs_gt = ground_truth[query_key_for_gt]
            print(f"Dokumen relevan (Ground Truth) untuk query \"{query_input}\": {relevant_docs_gt}\n")

            if vsm_has_results:
                p_vsm, r_vsm, f1_vsm = calculate_metrics(retrieved_docs_vsm, relevant_docs_gt)
                print("--- VSM Metrics ---")
                print(f"Precision: {p_vsm:.4f}")
                print(f"Recall:    {r_vsm:.4f}")
                print(f"F1-Score:  {f1_vsm:.4f}\n")
            else:
                print("--- VSM Metrics ---")
                print("Tidak ada hasil dari VSM untuk dievaluasi.\n")

            if bm25_has_results: 
                p_bm25, r_bm25, f1_bm25 = calculate_metrics(retrieved_docs_bm25, relevant_docs_gt)
                print("--- BM25 Metrics ---")
                print(f"Precision: {p_bm25:.4f}")
                print(f"Recall:    {r_bm25:.4f}")
                print(f"F1-Score:  {f1_bm25:.4f}\n")
            else:
                print("--- BM25 Metrics ---")
                print("Tidak ada hasil dari BM25 untuk dievaluasi (atau BM25 belum diimplementasikan/tidak mengembalikan hasil).\n")
            
            print("--- Perbandingan Model (Berdasarkan F1-Score) ---")
            if not vsm_has_results and not bm25_has_results:
                print("Kedua model tidak menghasilkan output. Tidak ada perbandingan.")
            elif f1_vsm > f1_bm25:
                print(f"VSM (F1: {f1_vsm:.4f}) berkinerja lebih baik daripada BM25 (F1: {f1_bm25:.4f}) untuk query ini.")
            elif f1_bm25 > f1_vsm:
                print(f"BM25 (F1: {f1_bm25:.4f}) berkinerja lebih baik daripada VSM (F1: {f1_vsm:.4f}) untuk query ini.")
            else:
                if vsm_has_results and bm25_has_results:
                    if p_vsm > p_bm25:
                        print(f"VSM dan BM25 memiliki F1-Score sama ({f1_vsm:.4f}). VSM unggul di Precision ({p_vsm:.4f} vs {p_bm25:.4f}).")
                    elif p_bm25 > p_vsm:
                        print(f"VSM dan BM25 memiliki F1-Score sama ({f1_bm25:.4f}). BM25 unggul di Precision ({p_bm25:.4f} vs {p_vsm:.4f}).")
                    else: # F1 sama, Precision sama
                        print(f"VSM dan BM25 memiliki performa F1-Score ({f1_vsm:.4f}) dan Precision ({p_vsm:.4f}) yang identik untuk query ini.")
                elif vsm_has_results: # Hanya VSM yang ada hasil (F1 mungkin 0 atau lebih)
                     print(f"VSM menghasilkan F1-Score {f1_vsm:.4f}. BM25 tidak menghasilkan output yang bisa dievaluasi atau F1-Score 0.")
                elif bm25_has_results: # Hanya BM25 yang ada hasil
                     print(f"BM25 menghasilkan F1-Score {f1_bm25:.4f}. VSM tidak menghasilkan output yang bisa dievaluasi atau F1-Score 0.")
        else:
            print(f"Ground truth untuk query \"{query_input}\" (key: \"{query_key_for_gt}\") tidak ditemukan.")
            print("Evaluasi tidak dapat dilakukan untuk query ini.")
            if len(ground_truth) > 0:
                print("\nQuery yang tersedia dalam ground_truth (lowercase) adalah:")
                for gt_q_val in ground_truth.keys():
                    print(f"- {gt_q_val}")
            else:
                print("Dictionary ground_truth saat ini kosong. Harap isi untuk melakukan evaluasi.")
            # Reset F1 scores jika tidak ada ground truth, agar tidak terbawa ke iterasi berikutnya jika tidak dihitung
            f1_vsm, f1_bm25 = 0.0, 0.0
        print("================================================")