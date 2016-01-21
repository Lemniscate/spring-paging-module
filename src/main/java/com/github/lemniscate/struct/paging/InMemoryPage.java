package com.github.lemniscate.struct.paging;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by dave on 7/7/15.
 */

/**
 * An in-memory implementation of {@link Page} to ease deserializing page from feign
 */
public class InMemoryPage<T> implements Page<T> {

    @Setter
    private List<T> content = new ArrayList<T>();

    @Getter @Setter
    @JsonProperty("page")
    private InMemoryPageRequest pageable;

    private int getTotal(){
        if( pageable != null ){
            return pageable.getTotalElements();
        }
        return content == null ? 0 : content.size();
    }

    // PageImpl methods

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.domain.Page#getTotalPages()
	 */
    @Override
    public int getTotalPages() {
        return getSize() == 0 ? 1 : (int) Math.ceil((double) getTotal() / (double) getSize());
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Page#getTotalElements()
     */
    @Override
    public long getTotalElements() {
        return !content.isEmpty() && pageable != null && pageable.getOffset() + pageable.getPageSize() > getTotal()
                ? pageable.getOffset() + content.size() : getTotal();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Slice#hasNext()
     */
    @Override
    public boolean hasNext() {
        return getNumber() + 1 < getTotalPages();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Slice#isLast()
     */
    @Override
    public boolean isLast() {
        return !hasNext();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        String contentType = "UNKNOWN";
        List<T> content = getContent();

        if (content.size() > 0) {
            contentType = content.get(0).getClass().getName();
        }

        return String.format("Page %s of %d containing %s instances", getNumber(), getTotalPages(), contentType);
    }

    // ***** CHUNK methods

    /*
         * (non-Javadoc)
         * @see org.springframework.data.domain.Slice#getNumber()
         */
    public int getNumber() {
        return pageable == null ? 0 : pageable.getPageNumber();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Slice#getSize()
     */
    public int getSize() {
        return pageable == null ? 0 : pageable.getPageSize();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Slice#getNumberOfElements()
     */
    public int getNumberOfElements() {
        return content.size();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Slice#hasPrevious()
     */
    public boolean hasPrevious() {
        return getNumber() > 0;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Slice#isFirst()
     */
    public boolean isFirst() {
        return !hasPrevious();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Slice#nextPageable()
     */
    public Pageable nextPageable() {
        return hasNext() ? pageable.next() : null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Slice#previousPageable()
     */
    public Pageable previousPageable() {

        if (hasPrevious()) {
            return pageable.previousOrFirst();
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Slice#hasContent()
     */
    public boolean hasContent() {
        return !content.isEmpty();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Slice#getContent()
     */
    public List<T> getContent() {
        return Collections.unmodifiableList(content);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.domain.Slice#getSort()
     */
    public Sort getSort() {
        return pageable == null ? null : pageable.getSort();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<T> iterator() {
        return content.iterator();
    }


    public static class InMemoryPageRequest implements Pageable{

        @Getter
        @Setter
        private int size, totalElements, number;

        @Getter
        @Setter
        private Sort sort;

        public InMemoryPageRequest() {}

        public InMemoryPageRequest(int number, int size, Sort sort) {

            if (number < 0) {
                throw new IllegalArgumentException("Page number must not be less than zero!");
            }

            if (size < 1) {
                throw new IllegalArgumentException("Page size must not be less than one!");
            }

            this.number = number;
            this.size = size;
            this.sort = sort;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.domain.Pageable#getPageSize()
         */
        public int getPageSize() {
            return size;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.domain.Pageable#getPageNumber()
         */
        public int getPageNumber() {
            return number;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.domain.Pageable#getOffset()
         */
        public int getOffset() {
            return number * size;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.domain.Pageable#hasPrevious()
         */
        public boolean hasPrevious() {
            return number > 0;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.domain.Pageable#previousOrFirst()
         */
        public Pageable previousOrFirst() {
            return hasPrevious() ? previous() : first();
        }

        /*
	 * (non-Javadoc)
	 * @see org.springframework.data.domain.Pageable#getSort()
	 */
        public Sort getSort() {
            return sort;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.domain.Pageable#next()
         */
        public Pageable next() {
            return new InMemoryPageRequest(getPageNumber() + 1, getPageSize(), getSort());
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.domain.AbstractPageRequest#previous()
         */
        public Pageable previous() {
            return getPageNumber() == 0 ? this : new InMemoryPageRequest(getPageNumber() - 1, getPageSize(), getSort());
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.domain.Pageable#first()
         */
        public Pageable first() {
            return new InMemoryPageRequest(0, getPageSize(), getSort());
        }

    }
}
