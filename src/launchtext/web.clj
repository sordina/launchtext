(ns launchtext.web)

(use 'compojure.core)
(use 'compojure.route)
(use 'compojure.handler)
(use 'ring.adapter.jetty)

(def html "<!DOCTYPE HTML>\n <html> <head> <meta http-equiv='content-type' content='text/html; charset=utf-8'> <title>Launcpad Message</title> </head> <body> <h1>Launchpad Message</h1> <form action='/' method='post'> <p><input type='text' name='message'></p> <p><input type='submit' value='Post!'></p> </form> </body> </html>")

(defroutes
  main-routes (GET       "/" [] html)
              (POST      "/" [] (do html))
              (not-found        "<h1>Page not found</h1>"))

(defn app [] (-> main-routes))

(defn start-web [] (run-jetty (app) {:port 9876  :join? false}))
