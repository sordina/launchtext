(ns launchtext.web)

(use 'compojure.core)
(use 'compojure.route)
(use 'compojure.handler)
(use 'ring.adapter.jetty)
(use 'ring.middleware.params)
(use 'launchtext.blit)

(def html "<!DOCTYPE HTML>\n <html> <head> <meta http-equiv='content-type' content='text/html; charset=utf-8'> <title>Launcpad Message</title> </head> <body> <h1>Launchpad Message</h1> <form action='/' method='post'> <p><input type='text' name='message'></p> <p><input type='submit' value='Post!'></p> </form> </body> </html>")

(defroutes
  main-routes (GET       "/" [] html)
              (POST      "/" [message] (do (prn (blit message)) html))
              (not-found "<h1>Page not found</h1>"))


(def app (-> #'main-routes wrap-params))

(defn start-web [] (run-jetty app {:port 9876  :join? false}))
