(ns get-token-api-droplet.core
  (:gen-class
   :name kf.session.ClojureGetTokenApiDroplet
   :extends atg.servlet.DynamoServlet
   :methods [[service [atg.servlet.DynamoHttpServletRequest atg.servlet.DynamoHttpServletResponse] void]
             [getAuthApiService [] kf.api.service.AuthApiService]
             [setAuthApiService [kf.api.service.AuthApiService] void]]
   :init init
   :state state))

(import 'kf.common.KFConstants)

(def SESSION_BEAN "sessionBean")
(def LOGOUT "logout")
(def FORCE_REFRESH "forceRefresh")
(def ACCESS_TOKEN "accessToken")
(def TOKEN_TYPE "tokenType")

(defn -init []
  [[] {:auth-api-service (atom nil)}])

(defn- -service [request response]
  (let [auth-api-session-bean (.getObjectParameter request SESSION_BEAN)
        force-refresh (boolean (.getParameter request FORCE_REFRESH))
        access-token (.getAccessToken auth-api-session-bean)]
    (if (or (blank? access-token)
            (.isAccessTokenExpired auth-api-session-bean)
            force-refresh)
      (doto request
        (.setParameter LOGOUT true)
        (.serviceLocalParameter KFConstants/OPARAM_EMPTY request response))
      (doto request
        (.setParameter TOKEN_TYPE (.getTokenType auth-api-session-bean))
        (.setParameter ACCESS_TOKEN access-token)
        (.serviceLocalParameter KFConstants/OPARAM_OUTPUT request response)))))

(defn- -getAuthApiService [this]
  (:auth-api-service @(.-state this)))

(defn- -setAuthApiService [this auth-api-service]
  (swap! (.-state this) assoc :auth-api-service auth-api-service))
