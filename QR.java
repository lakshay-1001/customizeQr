private WritableImage createCustomizeQr(String extractedText) {
        try {
            if (extractedText == null || extractedText.isEmpty()) {
                LOGGER.info("No valid text extracted from QR.");
                Image fallback = new Image(getClass().getResource("path to a fallback image").toExternalForm());//change with actual //image path
                return SwingFXUtils.toFXImage(SwingFXUtils.fromFXImage(fallback, null), null);
            }

            int qrSize = 5500;

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 0);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            BitMatrix matrix = new MultiFormatWriter().encode(
                    extractedText, BarcodeFormat.QR_CODE, 200, 200, hints);

            // Gradient colors
            Color startColor = web("#074F3E");
            Color midColor = web("#faab3f");// take it mostly according to your so that will enhance logo's visibility
            Color endColor = web("#D8303B");

            WritableImage qrWithGradient = applyGradientDotsToQR(matrix, qrSize, startColor, midColor, endColor);
            return addLogoToQR(qrWithGradient, "/com/shop/images/logo.png");

        } catch (Exception e) {
            LOGGER.error("Error generating QR Code: " + e.getMessage());
            Image fallback = new Image(getClass().getResource("path to a fallback image").toExternalForm());
            return SwingFXUtils.toFXImage(SwingFXUtils.fromFXImage(fallback, null), null);
        }
    }

    private WritableImage applyGradientDotsToQR(BitMatrix matrix, int qrSize, Color startColor, Color midColor, Color endColor) {
        int matrixSize = matrix.getWidth();
        int moduleSize = qrSize / matrixSize;

        WritableImage qrImage = new WritableImage(qrSize, qrSize);
        PixelWriter pixelWriter = qrImage.getPixelWriter();

        int dotRadius = moduleSize / 2;

        for (int y = 0; y < matrixSize; y++) {
            for (int x = 0; x < matrixSize; x++) {
                if (!matrix.get(x, y)) continue;

                int pixelX = x * moduleSize;
                int pixelY = y * moduleSize;

                int centerX = pixelX + moduleSize / 2;
                int centerY = pixelY + moduleSize / 2;

                double ratio = (double) y / matrixSize;
                Color color = (ratio < 0.5)
                        ? startColor.interpolate(midColor, ratio * 2)
                        : midColor.interpolate(endColor, (ratio - 0.5) * 2);

                for (int dx = -dotRadius; dx <= dotRadius; dx++) {
                    for (int dy = -dotRadius; dy <= dotRadius; dy++) {
                        if (dx * dx + dy * dy <= dotRadius * dotRadius) {
                            int drawX = centerX + dx;
                            int drawY = centerY + dy;
                            if (drawX >= 0 && drawY >= 0 && drawX < qrSize && drawY < qrSize) {
                                pixelWriter.setColor(drawX, drawY, color);
                            }
                        }
                    }
                }
            }
        }

        return qrImage;
    }


    private WritableImage addLogoToQR(WritableImage qrImage, String logoPath) {
        try {
            int qrSize = (int) qrImage.getWidth();
            int logoWidth = qrSize / 2;
            int logoHeight = qrSize / 4;

            BufferedImage qrBufferedImage = SwingFXUtils.fromFXImage(qrImage, null);
            Graphics2D graphics = qrBufferedImage.createGraphics();

            URL logoUrl = getClass().getResource(logoPath);
            if (logoUrl == null) {
                LOGGER.error("Logo image not found at: " + logoPath);
                return qrImage;
            }
            BufferedImage logoImage = ImageIO.read(logoUrl);

            java.awt.Image scaledLogo = logoImage.getScaledInstance((int) logoWidth, logoHeight,  java.awt.Image.SCALE_SMOOTH);
            BufferedImage finalLogo = new BufferedImage((int) logoWidth, logoHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D logoGraphics = finalLogo.createGraphics();
            logoGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            logoGraphics.drawImage(scaledLogo, 0, 0, null);
            logoGraphics.dispose();

            int centerX = (qrSize - logoWidth) / 2;
            int centerY = (qrSize - logoHeight) / 2;

            graphics.drawImage(finalLogo, (int) centerX, centerY, null);
            graphics.dispose();

            return SwingFXUtils.toFXImage(qrBufferedImage, null);

        } catch (Exception e) {
            LOGGER.error("Error adding transparent logo to QR Code: " + e.getMessage());
            return qrImage;
        }
    }
