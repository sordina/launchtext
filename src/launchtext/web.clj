(ns launchtext.web)

(use 'compojure.core)

(defroutes app (GET  "/" []     "<h1>Hello World</h1>")
               (POST "/" []     "<h1>Hello World</h1>")
               (route/not-found "<h1>Page not found</h1>"))
