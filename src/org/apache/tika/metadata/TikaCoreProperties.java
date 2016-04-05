/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.metadata;

/**
 * Contains a core set of basic Tika metadata properties, which all parsers
 *  will attempt to supply (where the file format permits). These are all
 *  defined in terms of other standard namespaces.
 *  
 * Users of Tika who wish to have consistent metadata across file formats
 *  can make use of these Properties, knowing that where present they will
 *  have consistent semantic meaning between different file formats. (No 
 *  matter if one file format calls it Title, another Long-Title and another
 *  Long-Name, if they all mean the same thing as defined by 
 *  {@link DublinCore#TITLE} then they will all be present as such)
 *
 * For now, most of these properties are composite ones including the deprecated
 *  non-prefixed String properties from the Metadata class. In Tika 2.0, most
 *  of these will revert back to simple assignments.
 * 
 * @since Apache Tika 1.2
 */
@SuppressWarnings("deprecation")
public interface TikaCoreProperties {

    /**
     * A file might contain different types of embedded documents.
     * The most common is the ATTACHEMENT.
     * An INLINE embedded resource should be used for embedded image
     * files that are used to render the page image (as in PDXObjImages in PDF files).
     * <p>
     * Not all parsers have yet implemented this. 
     *
     */
    public enum EmbeddedResourceType {
        INLINE,
        ATTACHMENT
    };

    /**
     * Use this to prefix metadata properties that store information
     * about the parsing process.  Users should be able to distinguish
     * between metadata that was contained within the document and
     * metadata about the parsing process.
     * In Tika 2.0 (or earlier?), let's change X-ParsedBy to X-TIKA-Parsed-By.
     */
    public static String TIKA_META_PREFIX = "X-TIKA"+Metadata.NAMESPACE_PREFIX_DELIMITER;

    /**
     * Use this to store parse exception information in the Metadata object.
     */
    public static String TIKA_META_EXCEPTION_PREFIX = TIKA_META_PREFIX+"EXCEPTION"+
            Metadata.NAMESPACE_PREFIX_DELIMITER;

    /**
     * This is currently used to identify Content-Type that may be
     * included within a document, such as in html documents
     * (e.g. <meta http-equiv="content-type" content="text/html; charset=UTF-8">)
     , or the value might come from outside the document.  This information
     * may be faulty and should be treated only as a hint.
     */
    public static final Property CONTENT_TYPE_HINT =
	Property.internalText("Content-Type"+"-Hint");

    /**
     * @see DublinCore#FORMAT
     */
    public static final Property FORMAT = Property.composite(Property.internalText(
																 "dc" + Metadata.NAMESPACE_PREFIX_DELIMITER + "format"), 
            new Property[] { Property.internalText(Metadata.FORMAT) });
    
   /**
    * @see DublinCore#IDENTIFIER
    */
	public static final Property IDENTIFIER = Property.composite(Property.internalText(
																	 "dc" + Metadata.NAMESPACE_PREFIX_DELIMITER + "identifier"), 
            new Property[] { Property.internalText(Metadata.IDENTIFIER) });
    
   /**
    * @see DublinCore#CONTRIBUTOR
    */
    public static final Property CONTRIBUTOR = Property.composite(Property.internalTextBag(
																	  "dc" + Metadata.NAMESPACE_PREFIX_DELIMITER + "contributor"), 
            new Property[] { Property.internalText(Metadata.CONTRIBUTOR) });
    
   /**
    * @see DublinCore#COVERAGE
    */
    public static final Property COVERAGE = Property.composite(Property.internalText(
																   "dc" + Metadata.NAMESPACE_PREFIX_DELIMITER + "coverage"), 
            new Property[] { Property.internalText(Metadata.COVERAGE) });
    
   /**
    * @see DublinCore#CREATOR
    */
    public static final Property CREATOR = Property.composite(Property.internalTextBag(
																  "dc" + Metadata.NAMESPACE_PREFIX_DELIMITER + "creator"), 
            new Property[] { 
																  Property.internalTextBag(
																	  "meta" + Metadata.NAMESPACE_PREFIX_DELIMITER + "author"),
                Property.internalTextBag(Metadata.CREATOR),
																  Property.internalTextBag("Author")
            });
    
    /**
     * @see Office#LAST_AUTHOR
     */
	public static final Property MODIFIER = Property.composite(Property.internalText(
																   "meta" + Metadata.NAMESPACE_PREFIX_DELIMITER + "last-author"), 
																new Property[] { Property.internalText("Last-Author") });
    
    /**
     * @see XMP#CREATOR_TOOL
     */
	public static final Property CREATOR_TOOL = Property.externalText("xmp" + Metadata.NAMESPACE_PREFIX_DELIMITER + "CreatorTool");
    
   /**
    * @see DublinCore#LANGUAGE
    */
    public static final Property LANGUAGE = Property.composite(Property.internalText(
																   "dc" + Metadata.NAMESPACE_PREFIX_DELIMITER + "language"), 
            new Property[] { Property.internalText(Metadata.LANGUAGE) });
    
   /**
    * @see DublinCore#PUBLISHER
    */
    public static final Property PUBLISHER = Property.composite(Property.internalText(
																	"dc" + Metadata.NAMESPACE_PREFIX_DELIMITER + "publisher"), 
            new Property[] { Property.internalText(Metadata.PUBLISHER) });
    
   /**
    * @see DublinCore#RELATION
    */
    public static final Property RELATION = Property.composite(Property.internalText(
																   "dc" + Metadata.NAMESPACE_PREFIX_DELIMITER + "relation"), 
            new Property[] { Property.internalText(Metadata.RELATION) });
    
   /**
    * @see DublinCore#RIGHTS
    */
    public static final Property RIGHTS = Property.composite(Property.internalText(
																 "dc" + Metadata.NAMESPACE_PREFIX_DELIMITER + "rights"), 
            new Property[] { Property.internalText(Metadata.RIGHTS) });
    
   /**
    * @see DublinCore#SOURCE
    */
    public static final Property SOURCE = Property.composite(Property.internalText(
																 "dc" + Metadata.NAMESPACE_PREFIX_DELIMITER + "source"), 
            new Property[] { Property.internalText(Metadata.SOURCE) });
    
   /**
    * @see DublinCore#TYPE
    */
    public static final Property TYPE = Property.composite(Property.internalText(
															   "dc" + Metadata.NAMESPACE_PREFIX_DELIMITER + "type"), 
            new Property[] { Property.internalText(Metadata.TYPE) });

    
    // Descriptive properties
    
    /**
     * @see DublinCore#TITLE
     */
    public static final Property TITLE = Property.composite(Property.internalText(
																"dc" + Metadata.NAMESPACE_PREFIX_DELIMITER + "title"), 
            new Property[] { Property.internalText(Metadata.TITLE) });
     
    /**
     * @see DublinCore#DESCRIPTION
     */
    public static final Property DESCRIPTION = Property.composite(Property.internalText(
																	  "dc" + Metadata.NAMESPACE_PREFIX_DELIMITER + "description"), 
            new Property[] { Property.internalText(Metadata.DESCRIPTION) });
     
    /**
     * @see DublinCore#SUBJECT
     * @see Office#KEYWORDS
     */
    public static final Property KEYWORDS = Property.composite(Property.internalTextBag(
																   "dc" + Metadata.NAMESPACE_PREFIX_DELIMITER + "subject"),
            new Property[] { 
																   Property.internalTextBag(
																	   "meta" + Metadata.NAMESPACE_PREFIX_DELIMITER + "keyword"), 
																   Property.internalTextBag("Keywords"),
                Property.internalTextBag(Metadata.SUBJECT)
            });
    
    // Date related properties
    
     /** 
      * @see DublinCore#DATE 
      * @see Office#CREATION_DATE 
      */
	public static final Property CREATED = Property.composite(Property.internalDate(
																  "dcterms" + Metadata.NAMESPACE_PREFIX_DELIMITER + "created"),
             new Property[] { 
																  Property.internalDate(
																	  "meta" + Metadata.NAMESPACE_PREFIX_DELIMITER + "creation-date"), 
																   Property.internalDate("Creation-Date")
             });
     
     /** 
      * @see DublinCore#MODIFIED
      * @see Metadata#DATE
      * @see Office#SAVE_DATE 
      */
	public static final Property MODIFIED = Property.composite(Property.internalDate(
																   "dcterms" + Metadata.NAMESPACE_PREFIX_DELIMITER + "modified"),
             new Property[] { 
                     Metadata.DATE,
																   Property.internalDate(
																	   "meta" + Metadata.NAMESPACE_PREFIX_DELIMITER + "save-date"), 
																	Property.internalDate("Last-Save-Date"), 
                     Property.internalText(Metadata.MODIFIED),
                     Property.internalText("Last-Modified")
             });
     
     /** @see Office#PRINT_DATE */
	public static final Property PRINT_DATE = Property.composite(Property.internalDate(
																	 "meta" + Metadata.NAMESPACE_PREFIX_DELIMITER + "print-date"), 
																  new Property[] { Property.internalDate("Last-Printed") });
     
     /**
      * @see XMP#METADATA_DATE
      */
	public static final Property METADATA_DATE = Property.externalDate("xmp" + Metadata.NAMESPACE_PREFIX_DELIMITER + "MetadataDate");
    
     
    // Geographic related properties
     
    /**
     * @see Geographic#LATITUDE
     */
    public static final Property LATITUDE = Property.internalReal("geo:lat");
    
    /**
     * @see Geographic#LONGITUDE
     */
    public static final Property LONGITUDE = Property.internalReal("geo:long");
    
    /**
     * @see Geographic#ALTITUDE
     */
    public static final Property ALTITUDE = Property.internalReal("geo:alt");
    
    
    // Comment and rating properties
    
    /**
     * @see XMP#RATING
     */
    public static final Property RATING = Property.externalReal("xmp" + Metadata.NAMESPACE_PREFIX_DELIMITER + "Rating");
    
    /** 
     * @see OfficeOpenXMLExtended#COMMENTS 
     */
    public static final Property COMMENTS = Property.composite(Property.externalTextBag(
																   "w" + Metadata.NAMESPACE_PREFIX_DELIMITER + "comments"), 
            new Property[] { 
																   Property.internalTextBag("comment"),
																   Property.internalTextBag("Comments")
            });
    
    // TODO: Remove transition properties in Tika 2.0
    
    /** 
     * @see DublinCore#SUBJECT 
     * @deprecated use TikaCoreProperties#KEYWORDS
     */
    @Deprecated
    public static final Property TRANSITION_KEYWORDS_TO_DC_SUBJECT = Property.composite(Property.internalTextBag(
																							"dc" + Metadata.NAMESPACE_PREFIX_DELIMITER + "subject"), 
																						new Property[] { Property.internalTextBag("Keywords") });
    
    /** 
     * @see OfficeOpenXMLExtended#COMMENTS 
     * @deprecated use TikaCoreProperties#DESCRIPTION
     */
    @Deprecated
    public static final Property TRANSITION_SUBJECT_TO_DC_DESCRIPTION = Property.composite(Property.internalText(
																							   "dc" + Metadata.NAMESPACE_PREFIX_DELIMITER + "description"), 
            new Property[] { Property.internalText(Metadata.SUBJECT) });
    
    /** 
     * @see DublinCore#TITLE 
     * @deprecated use TikaCoreProperties#TITLE
     */
    @Deprecated
    public static final Property TRANSITION_SUBJECT_TO_DC_TITLE = Property.composite(Property.internalText(
																						 "dc" + Metadata.NAMESPACE_PREFIX_DELIMITER + "title"), 
            new Property[] { Property.internalText(Metadata.SUBJECT) });
    
    /** 
     * @see OfficeOpenXMLCore#SUBJECT 
     * @deprecated use OfficeOpenXMLCore#SUBJECT
     */
    @Deprecated
    public static final Property TRANSITION_SUBJECT_TO_OO_SUBJECT = Property.composite(Property.externalText(
																						   "cp" + Metadata.NAMESPACE_PREFIX_DELIMITER + "subject"), 
            new Property[] { Property.internalText(Metadata.SUBJECT) });

    /**
     * See {@link #EMBEDDED_RESOURCE_TYPE}
     */
    public static final Property EMBEDDED_RESOURCE_TYPE = 
	Property.internalClosedChoise("embeddedResourceType", 
                    new String[]{EmbeddedResourceType.ATTACHMENT.toString(), EmbeddedResourceType.INLINE.toString()});

    
}
