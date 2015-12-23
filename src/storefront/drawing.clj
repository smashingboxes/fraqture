(ns storefront.drawing)

(defrecord Drawing [title setup-fn update-fn draw-fn exit-fn size features])
