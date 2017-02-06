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
(defonce path "/home/manish/manish/essays")

(def get-files-from-path (file-seq (io/file path)))

(defn only-files
  "Filter a sequence of files/directories by the .isFile property of
  java.io.File"
  [file-s]
  (filter #(.isFile %) file-s))

(defn names
  "Return the .getName property of a sequence of files"
  [file-s]
  (map #(.getName %) file-s))

(defn get-files-name []
  (sort
   (-> get-files-from-path
       only-files
       names)))

(defn get-file-contents [path file-name]
  (with-open [rdr (io/reader (str path "/" file-name)
                             :append true
                             :encoding "UTF-8")]
    #_(doseq [line (line-seq rdr)]
        (println line))
    (doall (line-seq rdr))))

(defn answer-hiccup [path]
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
                     (for [hd (take 5 (get-files-name))] ;; take only 5 files max
                       ^{:key hd}
                       [:th.table-th
                        (first (clj-string/split hd #"[.]")) ;; getting file name without extension
                        ])]]
                   [:tbody
                    [:tr
                     (for [file-name (take 5 (get-files-name))] ;; take only 5 file's contents max
                       ^{:key file-name}
                       [:td.table-td
                        [:div.table-td-div
                         (for [line (get-file-contents path file-name)]
                           ^{:key line}
                           [:p line])
                         #_ (slurp (str path "/" file-name))
                         ]])]]
                   ]]]]
               ))

(defn question-hiccup [path]
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
                   (answer-hiccup path)]]]]))


;; generate map with file-name and it's contents
(defn file-to-map [file-name file-contents-seq]
  {:file-name     file-name,
   :file-contents (into [] file-contents-seq)})

;; storing map of file-names and it's contents in vector
(defn data [path]
  (loop [return-data []
         files       (get-files-name)]
    (if (empty? files)
      return-data
      (recur (conj return-data
                   (file-to-map (first files)
                                (get-file-contents path
                                                   (first files))))
             (rest files)))))

;; manually generate JSON for file and its contents
(defn data-to-json [path]
  (generate-string (data path)
                   {:pretty true}))

;; define responce for success.
(defn html-response
  [html]
  {:status  200
   :body    html
   :headers {"Content-Type" "text/html"}
   })

;; Gather some data from the user to retain in their session.
(defn home-page
  [req]
  (html-response
   (question-hiccup path)))

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
