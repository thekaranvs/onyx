(ns onyx.log.commands.assign-bookkeeper-log-id
  (:require [clojure.core.async :refer [>!!]]
            [clojure.data :refer [diff]]
            [onyx.log.commands.common :as common]
            [onyx.log.entry :refer [create-log-entry]]
            [schema.core :as s]
            [onyx.schema :refer [Replica LogEntry Reactions ReplicaDiff State]]
            [onyx.extensions :as extensions]))

(s/defmethod extensions/apply-log-entry :signal-ready :- Replica
  [{:keys [args]} :- LogEntry replica]
  (update-in replica 
             [:state-logs (:job-id args) (:task-id args) (:slot-id args)]
             (fn [logs]
               (conj (vec logs) (:ledger-id args)))))

(s/defmethod extensions/replica-diff :signal-ready :- ReplicaDiff
  [{:keys [args]} :- LogEntry old new]
  (second (diff (:state-logs old) (:state-logs new))))

(s/defmethod extensions/reactions :signal-ready :- Reactions
  [{:keys [args]} :- LogEntry old new diff peer-args]
  [])

(s/defmethod extensions/fire-side-effects! :signal-ready :- State
  [{:keys [args message-id]} :- LogEntry old new diff state]
  state)
