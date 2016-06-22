(ns fraqture.exif
	(:require [clojure.java.io :as io]
            [clojure.set :refer :all]
            [clojure.string :refer [split]]
            [fraqture.stream :as stream]
            [fraqture.helpers :refer [image-extensions]])
	(:import [org.apache.commons.imaging Imaging])
	(:use [clj-xpath.core]))

(def ns-map {"x" "adobe:ns:meta/" 
			"rdf" "http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
			"xmp" "http://ns.adobe.com/xap/1.0/"
			"dc" "http://purl.org/dc/elements/1.1/"})

(defn getTextFromTag [xml xmlPath]
	"retrieve the first text node (content) from xml using the provided xmlPath"
	(with-namespace-context ns-map
		(first ($x:text* xmlPath xml))))

(defn xmp-xml [filename]
	"extract the XMP XML data from the file - specifically for PNG"
	(let [file (java.io.File. filename)
		  metadata (Imaging/getXmpXml file)]
		  (if ((complement nil?) metadata)
			(with-namespace-context ns-map 
			   {
			   	:author (getTextFromTag metadata "//dc:creator/*/rdf:li")
			    :title (getTextFromTag metadata "//dc:title/*/rdf:li")
			    :description (getTextFromTag metadata "//dc:description/*/rdf:li")
			    :date (getTextFromTag metadata "//xmp:CreateDate")
			    })
			({ :error "No metadata found." }))))

	