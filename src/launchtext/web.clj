(ns launchtext.web)

(use 'compojure.core)
(use 'compojure.route)
(use 'compojure.handler)
(use 'ring.adapter.jetty)
(use 'ring.middleware.params)
(use 'launchtext.blit)

(def html "<!DOCTYPE HTML>\n <html> <head> <meta http-equiv='content-type' content='text/html; charset=utf-8'> <title>Launcpad Message</title> </head> <body> <h1>Launchpad Message</h1> <p> Source available <a href='https://github.com/sordina/launchtext'>on Github</a> </p> <form action='/' method='post'> <p><input type='text' name='message'></p> <p><input type='submit' value='Post!'></p> </form> </body> </html>")

; And now the propper way!!

(defn create-routes [target]
     [ (GET       "/" [] html)
       (POST      "/" {params :params} (let [message (params "message")] (do (reset! target (blit message)) (prn message) html)))
       (not-found "<h1>Page not found</h1><p>Try the <a href='/'>homepage.</a></p>") ])

(defn create-local-app [target]
      (wrap-params (apply routes (create-routes target))))

(defn run-server-async [options]
  (let [ target    (or (:target options) (atom "Set :target!  "))
         port      (or (:port   options) 9876)
         local-app (create-local-app target)]

       (run-jetty local-app {:port port :join? false})))
