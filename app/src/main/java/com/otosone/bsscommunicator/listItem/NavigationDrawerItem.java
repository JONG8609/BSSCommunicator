package com.otosone.bsscommunicator.listItem;

public abstract class NavigationDrawerItem {
    private boolean isHeader;

    public NavigationDrawerItem(boolean isHeader) {
        this.isHeader = isHeader;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public static class NavigationHeader extends NavigationDrawerItem {
        private String title;
        private boolean isLarge;

        public NavigationHeader(String title, boolean isLarge) {
            super(true);  // true indicates it's a header
            this.title = title;
            this.isLarge = isLarge;
        }

        public String getTitle() {
            return title;
        }

        public boolean isLarge() {
            return isLarge;
        }
    }

    public static class NavigationItem extends NavigationDrawerItem {
        private int imageId;
        private String title;

        public NavigationItem(String title, int imageId) {
            super(false);  // false indicates it's not a header
            this.title = title;
            this.imageId = imageId;
        }

        public int getImageId() {
            return imageId;
        }

        public String getTitle() {
            return title;
        }
    }

    public static class NavigationDivider extends NavigationDrawerItem {
        public NavigationDivider() {
            super(true);  // true indicates it's a divider
        }
    }
}
