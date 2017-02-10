(ns clojure-read-files.service
  (:require [clojure.java.io                          :as io]
            [io.pedestal.http                         :as http]
            [io.pedestal.http.route                   :as route]
            [io.pedestal.http.body-params             :as body-params]
            [io.pedestal.http.ring-middlewares        :as middlewares]
            [ring.util.response                       :as ring-resp]
            [ring.middleware.session.cookie           :as cookie]
            ;;
            [ring.middleware.file                     :as ring-file]
            [hiccup.core                              :as hiccup]
            [clojure.string                           :as clj-string]
            [cheshire.core                            :refer :all] ;; for read/write JSON format
            ))


;; Question.
(def question " Write a program that reads 05 essays from plain-text files,
  then prints those essays as a table of 05 columns. Each essay should be printed
  in its own column. The essay should be printed as justified text with word wrap,
  without any loss of formatting (newline) around paragraphs.")

;; define path for accessing files from system.
;;(defonce path "/home/manish/manish/essays")
(def path (atom ""))

(defn get-all-inside-dir [dir-path]
  (file-seq (io/file dir-path)))

(defn only-files
  "Filter a sequence of files/directories by the .isFile property of
  java.io.File"
  [files-seq]
  (filter #(.isFile %) files-seq))

(defn file-names
  "Return the .getName property of a sequence of files"
  [files-seq]
  (map #(.getName %) files-seq))

(defn seq-file-name
  "Return only files name with lexical order"
  []
  (sort (file-names (only-files (get-all-inside-dir @path)))))

(defn get-file-contents
  "Return contents inside perticular file"
  [dir-path file-name]
  (with-open [rdr (io/reader (str dir-path "/" file-name)
                             :append true
                             :encoding "UTF-8")]
    #_(doseq [line (line-seq rdr)]
        (println line))
    (doall (line-seq rdr))))

(defn answer-hiccup
  ""
  []
  (hiccup/html ""
               [:html
                [:head
                 [:link {:media "screen",
                         :rel   "stylesheet",
                         :href  "/css/style.css"}]]
                [:body
                 [:div
                  [:table.table-table
                   [:thead
                    [:tr
                     (for [hd (take 5 (seq-file-name))] ;; take only 5 files max
                       ^{:key hd}
                       [:th.table-th
                        (first (clj-string/split hd #"[.]")) ;; getting file name without extension
                        ])]]
                   [:tbody
                    [:tr
                     (for [file-name (take 5 (seq-file-name))] ;; take only 5 file's contents max
                       ^{:key file-name}
                       [:td.table-td
                        [:div.table-td-div
                         (for [line (get-file-contents @path file-name)]
                           ^{:key line}
                           [:p line])
                         #_ (slurp (str path "/" file-name))
                         ]])]]
                   ]]]]
               ))

(defn ques-ans-hiccup
  ""
  []
  (hiccup/html ""
               [:html
                [:head
                 [:title "Read files in clojure"]]
                [:body
                 [:div
                  [:div
                   [:h4 "Question. "]
                   [:p question]]
                  [:div
                   [:h4 "Answer."]]
                  [:div
                   (answer-hiccup)]]]]))


;; generate map with file-name and it's contents
(defn file-to-map [file-name file-contents-seq]
  {:file-name     file-name,
   :file-contents (into [] file-contents-seq)})

;; storing map of file-names and it's contents in vector
(defn data []
  (loop [return-data []
         files       (seq-file-name)]
    (if (empty? files)
      return-data
      (recur (conj return-data
                   (file-to-map (first files)
                                (get-file-contents @path (first files))))
             (rest files)))))

;; manually generate JSON for file and its contents
(defn data-to-json []
  (generate-string (data)
                   {:pretty true}))

;; define html response for success.
(defn html-response
  [html]
  {:status  200
   :body    html
   :headers {"Content-Type" "text/html"}
   })

;; define JSON response, used for client side
#_(defn html-response
    [html]
    {:status  200
     :body    (data-to-json path)
     :headers {"Content-Type" "application/json"}
     })

;; Gather some data from the user to retain in their session.
(defn home-page
  [req]
  (html-response
   (ques-ans-hiccup)))

;; Set up routes to get all the above handlers accessible.
(def routes
  (let [session-interceptor (middlewares/session
                             {:store (cookie/cookie-store)})]
    (route/expand-routes
     [[["/" {:get `home-page}]]])))

(def service {:env                 :prod
              ::http/routes        routes
              ::http/resource-path "/public"
              ::http/type          :jetty
              ::http/port          8080})
