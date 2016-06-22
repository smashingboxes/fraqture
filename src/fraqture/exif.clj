(ns fraqture.exif
	(:require [clojure.java.io :as io]
            [clojure.set :refer :all]
            [clojure.string :refer [split]]
            [fraqture.stream :as stream]
            [fraqture.helpers :refer [image-extensions]]
            [clojure.xml :as xml]
            [clj-exif.core :as exif])
	(:import [org.apache.commons.imaging Imaging]
			 [org.apache.commons.imaging.common.bytesource ByteSourceFile])
	(:use [clj-xpath.core]))

(defn read-exif [filename]
	"read the exif data from a file - works on most formats EXCEPT PNG"
	(let [file (java.io.File. filename) 
		  metadata (exif/get-metadata file)]
		  (exif/read metadata)))

(def ns-map {"x" "adobe:ns:meta/" 
			"rdf" "http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
			"dc" "http://purl.org/dc/elements/1.1/"})

(defn xmp-xml [filename]
	"extract the XMP data from the file"
	(let [file (java.io.File. filename)
		  metadata (Imaging/getXmpXml file)]
		  (with-namespace-context ns-map 
		   {
		   	:author (first ($x:text+ "//dc:creator/*/rdf:li" metadata))
		    :description (first ($x:text+ "//dc:description/*/rdf:li" metadata))
		    :date (first ($x:text+ "//dc:date/*/rdf:li" metadata))
		    })))
	