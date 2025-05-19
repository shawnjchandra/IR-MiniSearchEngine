#include <iostream>
#include <vector>
#include <unordered_map>
#include <unordered_set>
#include <cmath>
#include <string>
#include <sstream>
#include <fstream>
#include <filesystem>
#include <algorithm>

using namespace std;
namespace fs = std::filesystem;

unordered_set<string> stopwords;
unordered_map<string, unordered_map<int, double>> idx; // term -> {doc_id -> normalized TF-IDF}
unordered_map<string, double> idf;
vector<string> documents;
vector<string> filenames;

void load_stopwords(const string &filename) {
    ifstream file(filename);
    string word;
    while (file >> word) {
        stopwords.insert(word);
    }
}

vector<string> tokenize(const string &text) {
    vector<string> tokens;
    string word;
    for (char ch : text) {
        if (isalnum(ch)) {
            word += tolower(ch);
        } else if (!word.empty()) {
            if (!stopwords.count(word)) {
                tokens.push_back(word);
            }
            word.clear();
        }
    }
    if (!word.empty() && !stopwords.count(word)) {
        tokens.push_back(word);
    }
    return tokens;
}

void read_documents(const string &folder) {
    for (const auto &entry : fs::directory_iterator(folder)) {
        if (entry.path().extension() == ".txt") {
            ifstream file(entry.path());
            stringstream buffer;
            buffer << file.rdbuf();
            documents.push_back(buffer.str());
            filenames.push_back(entry.path().filename().string());
        }
    }
}

void build_idx() {
    int N = documents.size();
    vector<unordered_map<string, int>> tfs(N);
    unordered_map<string, int> df;

    for (int i = 0; i < N; ++i) {
        auto tokens = tokenize(documents[i]);
        for (auto &term : tokens) {
            tfs[i][term]++;
        }
        for (auto &[term, _] : tfs[i]) {
            df[term]++;
        }
    }

    for (auto &[term, doc_freq] : df) {
        idf[term] = log((double)N / doc_freq);
    }

    for (int i = 0; i < N; ++i) {
        unordered_map<string, double> tfidf;
        double norm = 0.0;
        for (auto &[term, freq] : tfs[i]) {
            double weight = freq * idf[term];
            tfidf[term] = weight;
            norm += weight * weight;
        }
        norm = sqrt(norm);
        if (norm == 0.0) norm = 1e-10; // prevent division by zero
        for (auto &[term, weight] : tfidf) {
            idx[term][i] = weight / norm;
        }
    }
}

vector<pair<int, double>> score_documents(const string &query, int total_docs) {
    unordered_map<string, double> query_tf;
    auto tokens = tokenize(query);
    for (auto &term : tokens) {
        query_tf[term]++;
    }

    unordered_map<string, double> tfidf_query;
    double norm = 0.0;
    for (auto &[term, freq] : query_tf) {
        if (idf.count(term)) {
            double weight = freq * idf[term];
            tfidf_query[term] = weight;
            norm += weight * weight;
        }
    }

    norm = sqrt(norm);
    if (norm == 0.0) norm = 1e-10;
    for (auto &[term, weight] : tfidf_query) {
        tfidf_query[term] = weight / norm;
    }

    // Hitung skor dot product
    unordered_map<int, double> scores;
    for (auto &[term, q_weight] : tfidf_query) {
        if (idx.count(term)) {
            for (auto &[doc_id, d_weight] : idx[term]) {
                scores[doc_id] += q_weight * d_weight;
            }
        }
    }

    vector<pair<int, double>> result;
    for (int i = 0; i < total_docs; ++i) {
        double score = scores.count(i) ? scores[i] : 0.0;
        result.emplace_back(i, score);
    }

    // Urutkan berdasarkan skor (tertinggi ke terendah)
    sort(result.begin(), result.end(), [](auto &a, auto &b) {
        return a.second > b.second;
    });

    return result;
}


int main() {
    load_stopwords("resources/stopwords.txt");
    read_documents("resources/docs");
    build_idx();

    string query;
    cout << "Enter your query: ";
    getline(cin, query);

    auto results = score_documents(query, documents.size());

    cout << "\nTop matching documents:\n";
    for (auto &[doc_id, score] : results) {
        cout << "Doc: " << filenames[doc_id] << " | Score: " << score << endl;
    }

    return 0;
}