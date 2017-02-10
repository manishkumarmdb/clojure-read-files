(ns clojure-read-files.service-test
  (:require [clojure.test :refer :all]
            [io.pedestal.test :refer :all]
            [io.pedestal.http :as bootstrap]
            [clojure-read-files.service :as service]))

(def service
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))

(def str-response "<html><head><title>Read files in clojure</title></head><body><div><div><h4>Question. </h4><p> Write a program that reads 05 essays from plain-text files,\n  then prints those essays as a table of 05 columns. Each essay should be printed\n  in its own column. The essay should be printed as justified text with word wrap,\n  without any loss of formatting (newline) around paragraphs.</p></div><div><h4>Answer.</h4></div><div><html><head><link href=\"/css/style.css\" media=\"screen\" rel=\"stylesheet\" /></head><body><div><table class=\"table-table\"><thead><tr></tr></thead><tbody><tr></tr></tbody></table></div></body></html></div></div></body></html>")

(deftest home-page-test
  (is (=
       (:body (response-for service :get "/"))
       str-response))
  (is (=
       (:headers (response-for service :get "/"))
       {"Content-Security-Policy"           "object-src 'none'; script-src 'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:;",
        "Content-Type"                      "text/html",
        "Strict-Transport-Security"         "max-age=31536000; includeSubdomains",
        "X-Content-Type-Options"            "nosniff",
        "X-Download-Options"                "noopen",
        "X-Frame-Options"                   "DENY",
        "X-Permitted-Cross-Domain-Policies" "none",
        "X-XSS-Protection"                  "1; mode=block"}
       )))



